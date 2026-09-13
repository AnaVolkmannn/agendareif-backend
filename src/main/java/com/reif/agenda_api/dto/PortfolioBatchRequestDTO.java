package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PortfolioBatchRequestDTO(

        @NotEmpty(message = "Informe ao menos uma imagem")
        List<@NotBlank(message = "A URL da imagem é obrigatória") String> imageUrls

) {
}