package com.reif.agenda_api.service;

import com.reif.agenda_api.model.*;
import com.reif.agenda_api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final ProfessionalRepository professionalRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final PauseWeeklyScheduleRepository pauseWeeklyScheduleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final PauseExceptionRepository pauseExceptionRepository;
    private final SchedulingRepository schedulingRepository;

    public AvailabilityService(ProfessionalRepository professionalRepository,
                                ServiceOfferingRepository serviceOfferingRepository,
                                WeeklyScheduleRepository weeklyScheduleRepository,
                                PauseWeeklyScheduleRepository pauseWeeklyScheduleRepository,
                                ScheduleExceptionRepository scheduleExceptionRepository,
                                PauseExceptionRepository pauseExceptionRepository,
                                SchedulingRepository schedulingRepository) {
        this.professionalRepository = professionalRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.pauseWeeklyScheduleRepository = pauseWeeklyScheduleRepository;
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.pauseExceptionRepository = pauseExceptionRepository;
        this.schedulingRepository = schedulingRepository;
    }

    /** Janela de tempo simples, sempre dentro de um único dia. */
    private record Interval(LocalTime start, LocalTime end) {
        boolean isValid() {
            return start.isBefore(end);
        }
    }

    public List<LocalDateTime> getAvailableSlots(Long professionalId, Long serviceId, LocalDate date) {
        professionalRepository.findById(professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado: " + professionalId));

        ServiceOffering service = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado: " + serviceId));

        int durationMinutes = service.getDurationMinutes();

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<ScheduleException> exceptions = scheduleExceptionRepository
                .findByProfessionalIdAndStartTimeLessThanAndEndTimeGreaterThan(professionalId, dayEnd, dayStart);

        Optional<ScheduleException> specialException = exceptions.stream()
                .filter(e -> e.getType() == ScheduleException.ExceptionType.SPECIAL)
                .findFirst();

        List<Interval> baseWindows = new ArrayList<>();
        List<Interval> blockedIntervals = new ArrayList<>();

        if (specialException.isPresent()) {
            ScheduleException exception = specialException.get();
            baseWindows.add(clampToDay(exception.getStartTime(), exception.getEndTime(), date));

            for (PauseException pause : pauseExceptionRepository.findByScheduleExceptionId(exception.getId())) {
                blockedIntervals.add(clampToDay(pause.getStartTime(), pause.getEndTime(), date));
            }
        } else {
            int dayOfWeek = date.getDayOfWeek().getValue() % 7; // 0 = domingo ... 6 = sábado

            Optional<WeeklySchedule> weeklySchedule = weeklyScheduleRepository
                    .findByProfessionalIdAndDayOfWeek(professionalId, dayOfWeek);

            if (weeklySchedule.isEmpty() || !weeklySchedule.get().isActive()) {
                return List.of(); // dia fechado / sem expediente cadastrado
            }

            WeeklySchedule schedule = weeklySchedule.get();
            baseWindows.add(new Interval(schedule.getStartTime(), schedule.getEndTime()));

            for (PauseWeeklySchedule pause : pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(schedule.getId())) {
                blockedIntervals.add(new Interval(pause.getStartTime(), pause.getEndTime()));
            }
        }

        // DAY_OFF sempre bloqueia, independente de haver expediente especial ou padrão
        exceptions.stream()
                .filter(e -> e.getType() == ScheduleException.ExceptionType.DAY_OFF)
                .forEach(e -> blockedIntervals.add(clampToDay(e.getStartTime(), e.getEndTime(), date)));

        // Agendamentos ativos do profissional no dia também bloqueiam
        for (Scheduling scheduling : schedulingRepository.findByProfessionalIdAndScheduledAtBetween(professionalId, dayStart, dayEnd)) {
            if (scheduling.isCanceled()) {
                continue; // agendamento cancelado libera o horário automaticamente
            }
            LocalTime start = scheduling.getScheduledAt().toLocalTime();
            LocalTime end = start.plusMinutes(scheduling.getService().getDurationMinutes());
            blockedIntervals.add(new Interval(start, end));
        }

        List<Interval> freeIntervals = subtract(baseWindows, blockedIntervals);

        return generateSlots(freeIntervals, durationMinutes, date);
    }

    /** Recorta um período (possivelmente fora do dia) para dentro do dia informado. */
    private Interval clampToDay(LocalDateTime start, LocalDateTime end, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        LocalDateTime clampedStart = start.isBefore(dayStart) ? dayStart : start;
        LocalDateTime clampedEnd = end.isAfter(dayEnd) ? dayEnd : end;

        LocalTime startTime = clampedStart.toLocalTime();
        LocalTime endTime = clampedEnd.equals(dayEnd) ? LocalTime.MAX : clampedEnd.toLocalTime();

        return new Interval(startTime, endTime);
    }

    /** Subtrai os intervalos bloqueados das janelas base, devolvendo os intervalos livres restantes. */
    private List<Interval> subtract(List<Interval> baseWindows, List<Interval> blocked) {
        List<Interval> mergedBlocked = mergeIntervals(blocked);
        List<Interval> free = new ArrayList<>();

        for (Interval base : baseWindows) {
            LocalTime cursor = base.start();

            for (Interval block : mergedBlocked) {
                LocalTime blockStart = maxTime(block.start(), base.start());
                LocalTime blockEnd = minTime(block.end(), base.end());

                if (!blockStart.isBefore(blockEnd)) {
                    continue; // bloqueio não intersecta a janela base
                }
                if (blockStart.isAfter(cursor)) {
                    free.add(new Interval(cursor, blockStart));
                }
                if (blockEnd.isAfter(cursor)) {
                    cursor = blockEnd;
                }
            }

            if (cursor.isBefore(base.end())) {
                free.add(new Interval(cursor, base.end()));
            }
        }

        return free;
    }

    private List<Interval> mergeIntervals(List<Interval> intervals) {
        if (intervals.isEmpty()) return intervals;

        List<Interval> sorted = intervals.stream()
                .sorted(Comparator.comparing(Interval::start))
                .collect(Collectors.toList());

        List<Interval> merged = new ArrayList<>();
        Interval current = sorted.get(0);

        for (int i = 1; i < sorted.size(); i++) {
            Interval next = sorted.get(i);
            if (!next.start().isAfter(current.end())) {
                current = new Interval(current.start(), maxTime(current.end(), next.end()));
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    /** Gera slots consecutivos dentro das janelas livres, com passo igual à duração do serviço. */
    private List<LocalDateTime> generateSlots(List<Interval> freeIntervals, int durationMinutes, LocalDate date) {
        List<LocalDateTime> slots = new ArrayList<>();

        for (Interval interval : freeIntervals) {
            LocalTime cursor = interval.start();

            while (true) {
                LocalTime slotEnd = cursor.plusMinutes(durationMinutes);
                if (slotEnd.isBefore(cursor) || slotEnd.isAfter(interval.end())) {
                    break; // não cabe mais slot inteiro, ou passou da meia-noite
                }
                slots.add(LocalDateTime.of(date, cursor));
                cursor = slotEnd;
            }
        }

        return slots;
    }

    private LocalTime maxTime(LocalTime a, LocalTime b) { return a.isAfter(b) ? a : b; }
    private LocalTime minTime(LocalTime a, LocalTime b) { return a.isBefore(b) ? a : b; }
}