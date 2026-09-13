package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PortfolioReorderRequestDTO(

        @NotEmpty(message = "Informe a nova ordem das fotos")
        List<Long> orderedIds

) {
}