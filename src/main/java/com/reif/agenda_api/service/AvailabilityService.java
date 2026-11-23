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

        AgendaCarregada agenda = carregarAgenda(professionalId, date, date.plusDays(1));
        return slotsDoDia(agenda, service.getDurationMinutes(), date);
    }

    /**
     * Agenda do profissional num período, lida do banco de uma vez só.
     *
     * Calcular dia a dia refazendo as consultas custava ~5 idas ao banco por
     * dia — inviável para um mês inteiro. Aqui buscamos o período todo e o
     * cálculo de cada dia filtra em memória.
     */
    private record AgendaCarregada(
            Map<Integer, WeeklySchedule> horarioPorDiaDaSemana,
            Map<Long, List<PauseWeeklySchedule>> pausasDoHorario,
            List<ScheduleException> excecoes,
            Map<Long, List<PauseException>> pausasDaExcecao,
            List<Scheduling> agendamentos) {
    }

    private AgendaCarregada carregarAgenda(Long professionalId, LocalDate inicio, LocalDate fimExclusivo) {
        LocalDateTime de = inicio.atStartOfDay();
        LocalDateTime ate = fimExclusivo.atStartOfDay();

        Map<Integer, WeeklySchedule> horarios = new HashMap<>();
        Map<Long, List<PauseWeeklySchedule>> pausasDoHorario = new HashMap<>();
        for (WeeklySchedule horario : weeklyScheduleRepository.findByProfessionalIdOrderByDayOfWeekAsc(professionalId)) {
            horarios.put(horario.getDayOfWeek(), horario);
            pausasDoHorario.put(horario.getId(),
                    pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(horario.getId()));
        }

        List<ScheduleException> excecoes = scheduleExceptionRepository
                .findByProfessionalIdAndStartTimeLessThanAndEndTimeGreaterThan(professionalId, ate, de);

        Map<Long, List<PauseException>> pausasDaExcecao = new HashMap<>();
        for (ScheduleException excecao : excecoes) {
            pausasDaExcecao.put(excecao.getId(), pauseExceptionRepository.findByScheduleExceptionId(excecao.getId()));
        }

        List<Scheduling> agendamentos =
                schedulingRepository.findByProfessionalIdAndScheduledAtBetween(professionalId, de, ate);

        return new AgendaCarregada(horarios, pausasDoHorario, excecoes, pausasDaExcecao, agendamentos);
    }

    /** Horários livres de um dia, a partir da agenda já carregada. */
    private List<LocalDateTime> slotsDoDia(AgendaCarregada agenda, int durationMinutes, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<ScheduleException> exceptions = agenda.excecoes().stream()
                .filter(e -> e.getStartTime().isBefore(dayEnd) && e.getEndTime().isAfter(dayStart))
                .toList();

        Optional<ScheduleException> specialException = exceptions.stream()
                .filter(e -> e.getType() == ScheduleException.ExceptionType.SPECIAL)
                .findFirst();

        List<Interval> baseWindows = new ArrayList<>();
        List<Interval> blockedIntervals = new ArrayList<>();

        if (specialException.isPresent()) {
            ScheduleException exception = specialException.get();
            baseWindows.add(clampToDay(exception.getStartTime(), exception.getEndTime(), date));

            for (PauseException pause : agenda.pausasDaExcecao().getOrDefault(exception.getId(), List.of())) {
                blockedIntervals.add(clampToDay(pause.getStartTime(), pause.getEndTime(), date));
            }
        } else {
            int dayOfWeek = date.getDayOfWeek().getValue() % 7; // 0 = domingo ... 6 = sábado

            WeeklySchedule schedule = agenda.horarioPorDiaDaSemana().get(dayOfWeek);

            if (schedule == null || !schedule.isActive()) {
                return List.of(); // dia fechado / sem expediente cadastrado
            }

            baseWindows.add(new Interval(schedule.getStartTime(), schedule.getEndTime()));

            for (PauseWeeklySchedule pause : agenda.pausasDoHorario().getOrDefault(schedule.getId(), List.of())) {
                blockedIntervals.add(new Interval(pause.getStartTime(), pause.getEndTime()));
            }
        }

        // DAY_OFF sempre bloqueia, independente de haver expediente especial ou padrão
        exceptions.stream()
                .filter(e -> e.getType() == ScheduleException.ExceptionType.DAY_OFF)
                .forEach(e -> blockedIntervals.add(clampToDay(e.getStartTime(), e.getEndTime(), date)));

        // Agendamentos ativos do profissional no dia também bloqueiam
        for (Scheduling scheduling : agenda.agendamentos()) {
            LocalDateTime quando = scheduling.getScheduledAt();
            if (quando.isBefore(dayStart) || !quando.isBefore(dayEnd)) {
                continue; // de outro dia do período carregado
            }
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

    /**
     * Dias de um mês em que sobra pelo menos um horário livre.
     *
     * O calendário do cliente precisa saber isso de todos os dias de uma vez
     * para marcar os disponíveis e desabilitar o resto. Sem esta rota, o front
     * teria que chamar getAvailableSlots uma vez por dia — ~30 requisições a
     * cada troca de mês.
     *
     * Dias passados são ignorados: não há como agendar para trás.
     */
    public List<Integer> getDaysWithAvailability(Long professionalId, Long serviceId, int year, int month) {
        professionalRepository.findById(professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado: " + professionalId));

        ServiceOffering service = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado: " + serviceId));

        LocalDate primeiroDia = LocalDate.of(year, month, 1);
        LocalDate hoje = LocalDate.now();

        // Mês inteiro numa carga só; o resto é cálculo em memória.
        AgendaCarregada agenda = carregarAgenda(professionalId, primeiroDia, primeiroDia.plusMonths(1));

        List<Integer> dias = new ArrayList<>();
        for (int dia = 1; dia <= primeiroDia.lengthOfMonth(); dia++) {
            LocalDate data = LocalDate.of(year, month, dia);
            if (data.isBefore(hoje)) continue;

            if (!slotsDoDia(agenda, service.getDurationMinutes(), data).isEmpty()) {
                dias.add(dia);
            }
        }
        return dias;
    }

    private LocalTime maxTime(LocalTime a, LocalTime b) { return a.isAfter(b) ? a : b; }
    private LocalTime minTime(LocalTime a, LocalTime b) { return a.isBefore(b) ? a : b; }
}