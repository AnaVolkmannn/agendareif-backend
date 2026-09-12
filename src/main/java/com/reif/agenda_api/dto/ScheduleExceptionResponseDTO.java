package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.ScheduleException;

import java.time.LocalDateTime;

public record ScheduleExceptionResponseDTO(
        Long id,
        Long professionalId,
        ScheduleException.ExceptionType type,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer breakBetween
) {
    public static ScheduleExceptionResponseDTO fromEntity(ScheduleException exception) {
        return new ScheduleExceptionResponseDTO(
                exception.getId(),
                exception.getProfessional().getId(),
                exception.getType(),
                exception.getStartTime(),
                exception.getEndTime(),
                exception.getBreakBetween()
        );
    }
}