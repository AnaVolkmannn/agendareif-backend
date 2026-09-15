package com.reif.agenda_api.service;

import com.reif.agenda_api.model.PauseException;
import com.reif.agenda_api.model.ScheduleException;
import com.reif.agenda_api.repository.PauseExceptionRepository;
import com.reif.agenda_api.repository.ScheduleExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PauseExceptionService {

    private final PauseExceptionRepository pauseExceptionRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;

    public PauseExceptionService(PauseExceptionRepository pauseExceptionRepository,
                                  ScheduleExceptionRepository scheduleExceptionRepository) {
        this.pauseExceptionRepository = pauseExceptionRepository;
        this.scheduleExceptionRepository = scheduleExceptionRepository;
    }

    @Transactional
    public PauseException create(Long scheduleExceptionId, PauseException pause) {
        ScheduleException exception = scheduleExceptionRepository.findById(scheduleExceptionId)
                .orElseThrow(() -> new IllegalArgumentException("Exceção de agenda não encontrada."));

        validate(exception, pause);

        pause.setScheduleException(exception);
        return pauseExceptionRepository.save(pause);
    }

    public List<PauseException> findByScheduleException(Long scheduleExceptionId) {
        return pauseExceptionRepository.findByScheduleExceptionId(scheduleExceptionId);
    }

    @Transactional
    public void delete(Long id) {
        PauseException pause = pauseExceptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Intervalo não encontrado."));
        pauseExceptionRepository.delete(pause);
    }

    /**
     * Garante que o intervalo é válido:
     * - Só pode existir em exceções do tipo SPECIAL (Horário especial).
     * - A exceção pai precisa já ter início/fim definidos.
     * - Precisa ter início e fim, com fim posterior ao início.
     * - Precisa estar contido dentro do início/fim do horário especial ao qual pertence.
     * - Não pode se sobrepor a outro intervalo já cadastrado na mesma exceção.
     */
    private void validate(ScheduleException exception, PauseException pause) {
        if (exception.getType() != ScheduleException.ExceptionType.SPECIAL) {
            throw new IllegalArgumentException("Intervalos só podem ser cadastrados em horários especiais.");
        }

        if (exception.getStartTime() == null || exception.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "O horário especial precisa ter início e fim definidos antes de cadastrar um intervalo."
            );
        }

        if (pause.getStartTime() == null || pause.getEndTime() == null) {
            throw new IllegalArgumentException("O intervalo precisa ter início e fim.");
        }

        if (!pause.getEndTime().isAfter(pause.getStartTime())) {
            throw new IllegalArgumentException("O fim do intervalo deve ser posterior ao início.");
        }

        if (pause.getStartTime().isBefore(exception.getStartTime())
                || pause.getEndTime().isAfter(exception.getEndTime())) {
            throw new IllegalArgumentException(
                    "O intervalo deve estar dentro do horário especial selecionado."
            );
        }

        checkNoOverlappingPause(exception.getId(), pause);
    }

    private void checkNoOverlappingPause(Long scheduleExceptionId, PauseException pause) {
        List<PauseException> existing = pauseExceptionRepository.findByScheduleExceptionId(scheduleExceptionId);

        boolean hasOverlap = existing.stream().anyMatch(other ->
                pause.getStartTime().isBefore(other.getEndTime())
                        && other.getStartTime().isBefore(pause.getEndTime())
        );

        if (hasOverlap) {
            throw new IllegalArgumentException("Já existe um intervalo cadastrado nesse horário.");
        }
    }
}