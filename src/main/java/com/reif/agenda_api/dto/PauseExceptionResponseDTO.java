package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.PauseException;

import java.time.LocalDateTime;

public record PauseExceptionResponseDTO(
        Long id,
        Long scheduleExceptionId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    public static PauseExceptionResponseDTO fromEntity(PauseException pause) {
        return new PauseExceptionResponseDTO(
                pause.getId(),
                pause.getScheduleException().getId(),
                pause.getStartTime(),
                pause.getEndTime()
        );
    }
}