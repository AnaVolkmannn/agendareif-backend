package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.Portfolio;

public record PortfolioResponseDTO(
        Long id,
        Long professionalId,
        String imageUrl,
        Integer sortOrder
) {
    public static PortfolioResponseDTO fromEntity(Portfolio portfolio) {
        return new PortfolioResponseDTO(
                portfolio.getId(),
                portfolio.getProfessional().getId(),
                portfolio.getImageUrl(),
                portfolio.getSortOrder()
        );
    }
}