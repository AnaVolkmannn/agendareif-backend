package com.reif.agenda_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record WeeklyScheduleRequestDTO(

        @NotNull(message = "O dia da semana é obrigatório")
        @Min(value = 0, message = "O dia da semana vai de 0 (domingo) a 6 (sábado)")
        @Max(value = 6, message = "O dia da semana vai de 0 (domingo) a 6 (sábado)")
        Integer dayOfWeek,

        /** Quando null, o dia fica fechado (RF18). */
        Boolean active,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime endTime

) {
}
