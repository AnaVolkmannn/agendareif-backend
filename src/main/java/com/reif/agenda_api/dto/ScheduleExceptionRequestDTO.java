package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.ScheduleException;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleExceptionRequestDTO(
        @NotNull ScheduleException.ExceptionType type,
        @NotNull LocalDateTime startTime,
        LocalDateTime endTime
) {
}