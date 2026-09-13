package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotBlank;

public record PortfolioRequestDTO(

        @NotBlank(message = "A URL da imagem é obrigatória")
        String imageUrl

) {
}