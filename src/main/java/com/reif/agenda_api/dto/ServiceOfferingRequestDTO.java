package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ServiceOfferingRequestDTO(

        @NotBlank(message = "O nome do serviço é obrigatório")
        String name,

        /** Mantido por compatibilidade; quando photos vem preenchido, é ignorado. */
        String imageUrl,

        @Size(max = MAX_PHOTOS, message = "Um serviço pode ter no máximo 5 fotos")
        List<@NotBlank(message = "A URL da foto não pode ser vazia") String> photos,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal price,

        String description,

        @NotNull(message = "A duração é obrigatória")
        @Positive(message = "A duração deve ser maior que zero")
        Integer durationMinutes,

        boolean acceptsInspiration

) {
    public static final int MAX_PHOTOS = 5;
}