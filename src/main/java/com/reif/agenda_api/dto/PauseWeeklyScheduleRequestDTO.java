package com.reif.agenda_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record PauseWeeklyScheduleRequestDTO(

        @NotNull(message = "O início do intervalo é obrigatório")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime startTime,

        @NotNull(message = "O fim do intervalo é obrigatório")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime endTime
) {
}
