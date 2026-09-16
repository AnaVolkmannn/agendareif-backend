package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PauseExceptionRequestDTO(
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
}