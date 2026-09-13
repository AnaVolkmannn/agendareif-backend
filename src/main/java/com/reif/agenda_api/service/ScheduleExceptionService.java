package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.ScheduleException;
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

    public ScheduleExceptionService(ScheduleExceptionRepository scheduleExceptionRepository,
                                     ProfessionalRepository professionalRepository) {
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.professionalRepository = professionalRepository;
    }

    @Transactional
    public ScheduleException create(Long professionalId, ScheduleException exception) {
        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));

        normalizeAndValidate(exception);
        checkNoOverlappingException(professionalId, exception);

        // TODO: reativar quando Appointment (Model + Repository) existir
        // checkNoConflictingAppointments(professionalId, exception);

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

    private void normalizeAndValidate(ScheduleException exception) {
        if (exception.getStartTime() == null) {
            throw new IllegalArgumentException("A data da exceção é obrigatória.");
        }

        if (exception.getType() == ScheduleException.ExceptionType.DAY_OFF) {
            LocalDate day = exception.getStartTime().toLocalDate();
            exception.setStartTime(day.atStartOfDay());
            exception.setEndTime(day.plusDays(1).atStartOfDay());

        } else if (exception.getType() == ScheduleException.ExceptionType.SPECIAL) {
            if (exception.getEndTime() == null) {
                throw new IllegalArgumentException("Horário especial requer horário de fim.");
            }
            if (!exception.getEndTime().isAfter(exception.getStartTime())) {
                throw new IllegalArgumentException("O horário de fim deve ser posterior ao horário de início.");
            }
        }
    }

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

    // TODO: descomentar e implementar quando Appointment existir
    /*
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
    */
}