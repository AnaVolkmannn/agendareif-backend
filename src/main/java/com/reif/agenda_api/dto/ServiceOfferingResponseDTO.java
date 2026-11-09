package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.ServiceOffering;

import java.math.BigDecimal;
import java.util.List;

public record ServiceOfferingResponseDTO(
        Long id,
        Long professionalId,
        String name,
        String imageUrl,
        List<String> photos,
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
                photosDe(service),
                service.getPrice(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.isAcceptsInspiration()
        );
    }

    /**
     * Serviços cadastrados antes da lista só têm imageUrl. Devolvê-lo como
     * foto única evita que a imagem deles desapareça da tela, sem precisar
     * de script de migração.
     */
    private static List<String> photosDe(ServiceOffering service) {
        if (!service.getPhotos().isEmpty()) {
            return List.copyOf(service.getPhotos());
        }
        return service.getImageUrl() == null ? List.of() : List.of(service.getImageUrl());
    }
}