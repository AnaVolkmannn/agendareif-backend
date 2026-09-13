package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotNull;

public record BreakBetweenRequestDTO(

        @NotNull(message = "O id do profissional é obrigatório")
        Long professionalId,

        Integer breakDuration

) {
}