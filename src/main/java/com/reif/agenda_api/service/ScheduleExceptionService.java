package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.ScheduleException;
import com.reif.agenda_api.repository.AppointmentRepository;
import com.reif.agenda_api.repository.ProfessionalRepository;
import com.reif.agenda_api.repository.ScheduleExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleExceptionService {

    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final ProfessionalRepository professionalRepository;
    private final AppointmentRepository appointmentRepository;

    public ScheduleExceptionService(ScheduleExceptionRepository scheduleExceptionRepository,
                                     ProfessionalRepository professionalRepository,
                                     AppointmentRepository appointmentRepository) {
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.professionalRepository = professionalRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public ScheduleException create(Long professionalId, ScheduleException exception) {
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));

        normalizeAndValidate(exception);
        checkNoOverlappingException(professionalId, exception);
        checkNoConflictingAppointments(professionalId, exception);

        exception.setProfessional(professional);
        return scheduleExceptionRepository.save(exception);
    }

    public List<ScheduleException> findByProfessional(Long professionalId) {
        return scheduleExceptionRepository.findByProfessionalId(professionalId);
    }

    @Transactional
    public void delete(Long id) {
        ScheduleException exception = scheduleExceptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exceção de agenda não encontrada."));
        scheduleExceptionRepository.delete(exception);
    }

    /**
     * Valida e normaliza a exceção conforme o tipo:
     * - DAY_OFF (Folga): exige apenas o dia selecionado; passa a cobrir
     *   00:00 até 00:00 do dia seguinte (o dia inteiro).
     * - SPECIAL (Horário especial): exige início e fim, com fim após início.
     */
    private void normalizeAndValidate(ScheduleException exception) {
        if (exception.getStartTime() == null) {
            throw new IllegalArgumentException("A data da exceção é obrigatória.");
        }

        if (exception.getType() == ScheduleException.ExceptionType.DAY_OFF) {
            LocalDate day = exception.getStartTime().toLocalDate();
            exception.setStartTime(day.atStartOfDay());
            exception.setEndTime(day.plusDays(1).atStartOfDay());
            exception.setBreakBetween(null);

        } else if (exception.getType() == ScheduleException.ExceptionType.SPECIAL) {
            if (exception.getEndTime() == null) {
                throw new IllegalArgumentException("Horário especial requer horário de fim.");
            }
            if (!exception.getEndTime().isAfter(exception.getStartTime())) {
                throw new IllegalArgumentException("O horário de fim deve ser posterior ao horário de início.");
            }
        }
    }

    /**
     * Impede o cadastro se já existir outra exceção (folga ou horário especial)
     * do mesmo profissional que se sobreponha ao período informado.
     */
    private void checkNoOverlappingException(Long professionalId, ScheduleException exception) {
        boolean hasOverlap = scheduleExceptionRepository
                .existsByProfessionalIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        professionalId, exception.getEndTime(), exception.getStartTime()
                );

        if (hasOverlap) {
            throw new IllegalStateException(
                    "Já existe uma folga ou horário especial cadastrado nesse dia/horário."
            );
        }
    }

    /**
     * Impede o cadastro se já existir algum agendamento (não cancelado) do
     * profissional dentro do período afetado pela exceção.
     */
    private void checkNoConflictingAppointments(Long professionalId, ScheduleException exception) {
        boolean hasConflict = appointmentRepository.existsByProfessionalIdAndDateTimeBetweenAndCanceledFalse(
                professionalId, exception.getStartTime(), exception.getEndTime()
        );

        if (hasConflict) {
            throw new IllegalStateException(
                    "Não é possível cadastrar essa exceção: já existem agendamentos nesse período."
            );
        }
    }
}