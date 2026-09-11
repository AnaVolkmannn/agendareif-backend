package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.WeeklySchedule;
import com.reif.agenda_api.repository.WeeklyScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final ProfessionalService professionalService;

    public WeeklyScheduleService(WeeklyScheduleRepository weeklyScheduleRepository,
                                  ProfessionalService professionalService) {
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.professionalService = professionalService;
    }

    @Transactional
    public WeeklySchedule create(Long professionalId, WeeklySchedule data) {
        Professional professional = professionalService.findById(professionalId);

        if (weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(professionalId, data.getDayOfWeek())) {
            throw new IllegalArgumentException("Esse dia da semana já está cadastrado para o profissional.");
        }

        data.setProfessional(professional);
        validate(data);
        return weeklyScheduleRepository.save(data);
    }

    public List<WeeklySchedule> findAllByProfessional(Long professionalId) {
        professionalService.findById(professionalId);
        return weeklyScheduleRepository.findByProfessionalIdOrderByDayOfWeekAsc(professionalId);
    }

    public WeeklySchedule findById(Long professionalId, Long id) {
        return weeklyScheduleRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Horário semanal não encontrado."));
    }

    public WeeklySchedule findByDayOfWeek(Long professionalId, Integer dayOfWeek) {
        return weeklyScheduleRepository.findByProfessionalIdAndDayOfWeek(professionalId, dayOfWeek)
                .orElseThrow(() -> new IllegalArgumentException("Horário semanal não encontrado."));
    }

    @Transactional
    public WeeklySchedule update(Long professionalId, Long id, WeeklySchedule data) {
        WeeklySchedule weeklySchedule = findById(professionalId, id);

        // Trocar o dia da semana não pode colidir com outro dia já cadastrado.
        if (!weeklySchedule.getDayOfWeek().equals(data.getDayOfWeek())
                && weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(professionalId, data.getDayOfWeek())) {
            throw new IllegalArgumentException("Esse dia da semana já está cadastrado para o profissional.");
        }

        weeklySchedule.setDayOfWeek(data.getDayOfWeek());
        weeklySchedule.setActive(data.isActive());
        weeklySchedule.setStartTime(data.getStartTime());
        weeklySchedule.setEndTime(data.getEndTime());
        weeklySchedule.setBreakBetween(data.getBreakBetween());

        validate(weeklySchedule);
        return weeklyScheduleRepository.save(weeklySchedule);
    }

    /**
     * Cadastro em lote (RF24): substitui o horário da semana inteiro pelo
     * que veio na requisição.
     */
    @Transactional
    public List<WeeklySchedule> replaceWeek(Long professionalId, List<WeeklySchedule> days) {
        Professional professional = professionalService.findById(professionalId);

        Set<Integer> diasRecebidos = new HashSet<>();
        for (WeeklySchedule day : days) {
            if (!diasRecebidos.add(day.getDayOfWeek())) {
                throw new IllegalArgumentException("O dia da semana " + day.getDayOfWeek() + " veio repetido.");
            }
            day.setProfessional(professional);
            validate(day);
        }

        // O flush garante que os deletes vão pro banco antes dos inserts,
        // senão a unique (professional_id, day_of_week) estoura.
        weeklyScheduleRepository.deleteAll(
                weeklyScheduleRepository.findByProfessionalIdOrderByDayOfWeekAsc(professionalId)
        );
        weeklyScheduleRepository.flush();

        return weeklyScheduleRepository.saveAll(days);
    }

    @Transactional
    public void delete(Long professionalId, Long id) {
        WeeklySchedule weeklySchedule = findById(professionalId, id);
        weeklyScheduleRepository.delete(weeklySchedule);
    }

    /**
     * Um dia fechado não precisa de horário. Já um dia aberto precisa de
     * início e fim coerentes, senão a agenda gera horários inválidos.
     */
    private void validate(WeeklySchedule weeklySchedule) {
        if (weeklySchedule.getBreakBetween() == null) {
            weeklySchedule.setBreakBetween(0);
        }

        if (!weeklySchedule.isActive()) {
            return;
        }

        if (weeklySchedule.getStartTime() == null || weeklySchedule.getEndTime() == null) {
            throw new IllegalArgumentException("Informe o horário de início e fim para um dia aberto.");
        }

        if (!weeklySchedule.getStartTime().isBefore(weeklySchedule.getEndTime())) {
            throw new IllegalArgumentException("O horário de início deve ser anterior ao horário de fim.");
        }
    }
}
