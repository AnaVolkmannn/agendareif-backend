package com.reif.agenda_api.service;

import com.reif.agenda_api.model.PauseWeeklySchedule;
import com.reif.agenda_api.model.WeeklySchedule;
import com.reif.agenda_api.repository.PauseWeeklyScheduleRepository;
import com.reif.agenda_api.repository.WeeklyScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PauseWeeklyScheduleService {

    private final PauseWeeklyScheduleRepository pauseWeeklyScheduleRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public PauseWeeklyScheduleService(PauseWeeklyScheduleRepository pauseWeeklyScheduleRepository,
                                       WeeklyScheduleRepository weeklyScheduleRepository) {
        this.pauseWeeklyScheduleRepository = pauseWeeklyScheduleRepository;
        this.weeklyScheduleRepository = weeklyScheduleRepository;
    }

    @Transactional
    public PauseWeeklySchedule create(Long weeklyScheduleId, PauseWeeklySchedule pause) {
        WeeklySchedule weeklySchedule = findWeeklySchedule(weeklyScheduleId);

        validate(weeklySchedule, pause);

        pause.setWeeklySchedule(weeklySchedule);
        return pauseWeeklyScheduleRepository.save(pause);
    }

    public List<PauseWeeklySchedule> findByWeeklySchedule(Long weeklyScheduleId) {
        findWeeklySchedule(weeklyScheduleId);
        return pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(weeklyScheduleId);
    }

    @Transactional
    public void delete(Long id) {
        PauseWeeklySchedule pause = pauseWeeklyScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Intervalo não encontrado."));
        pauseWeeklyScheduleRepository.delete(pause);
    }

    private WeeklySchedule findWeeklySchedule(Long weeklyScheduleId) {
        return weeklyScheduleRepository.findById(weeklyScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Horário semanal não encontrado."));
    }

    /**
     * Garante que o intervalo é válido:
     * - Só pode existir em dia aberto (dia fechado não tem expediente).
     * - O dia precisa já ter início/fim definidos.
     * - Precisa ter início e fim, com fim posterior ao início.
     * - Precisa estar contido dentro do turno do dia ao qual pertence.
     * - Não pode se sobrepor a outro intervalo já cadastrado no mesmo dia.
     */
    private void validate(WeeklySchedule weeklySchedule, PauseWeeklySchedule pause) {
        if (!weeklySchedule.isActive()) {
            throw new IllegalArgumentException("Intervalos só podem ser cadastrados em dias abertos.");
        }

        if (weeklySchedule.getStartTime() == null || weeklySchedule.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "O dia precisa ter início e fim definidos antes de cadastrar um intervalo."
            );
        }

        if (pause.getStartTime() == null || pause.getEndTime() == null) {
            throw new IllegalArgumentException("O intervalo precisa ter início e fim.");
        }

        if (!pause.getEndTime().isAfter(pause.getStartTime())) {
            throw new IllegalArgumentException("O fim do intervalo deve ser posterior ao início.");
        }

        if (pause.getStartTime().isBefore(weeklySchedule.getStartTime())
                || pause.getEndTime().isAfter(weeklySchedule.getEndTime())) {
            throw new IllegalArgumentException("O intervalo deve estar dentro do horário de trabalho do dia.");
        }

        checkNoOverlappingPause(weeklySchedule.getId(), pause);
    }

    private void checkNoOverlappingPause(Long weeklyScheduleId, PauseWeeklySchedule pause) {
        List<PauseWeeklySchedule> existing =
                pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(weeklyScheduleId);

        boolean hasOverlap = existing.stream().anyMatch(other ->
                pause.getStartTime().isBefore(other.getEndTime())
                        && other.getStartTime().isBefore(pause.getEndTime())
        );

        if (hasOverlap) {
            throw new IllegalArgumentException("Já existe um intervalo cadastrado nesse horário.");
        }
    }
}
