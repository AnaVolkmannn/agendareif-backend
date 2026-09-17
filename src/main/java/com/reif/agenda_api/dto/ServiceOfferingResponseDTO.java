package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.ServiceOffering;

import java.math.BigDecimal;

public record ServiceOfferingResponseDTO(
        Long id,
        Long professionalId,
        String name,
        String imageUrl,
        BigDecimal price,
        String description,
        Integer durationMinutes,
        boolean acceptsInspiration
) {

    public static ServiceOfferingResponseDTO fromEntity(ServiceOffering service) {
        return new ServiceOfferingResponseDTO(
                service.getId(),
                service.getProfessional().getId(),
                service.getName(),
                service.getImageUrl(),
                service.getPrice(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.isAcceptsInspiration()
        );
    }
}