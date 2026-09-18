package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ServiceOfferingRequestDTO(

        @NotBlank(message = "O nome do serviço é obrigatório")
        String name,

        String imageUrl,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal price,

        String description,

        @NotNull(message = "A duração é obrigatória")
        @Positive(message = "A duração deve ser maior que zero")
        Integer durationMinutes,

        boolean acceptsInspiration

) {
}