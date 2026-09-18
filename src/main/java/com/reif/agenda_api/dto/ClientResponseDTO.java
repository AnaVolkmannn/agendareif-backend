package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.Client;

public record ClientResponseDTO(
        Long id,
        String name,
        String email,
        String phone,
        String observation
) {

    public static ClientResponseDTO fromEntity(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getObservation()
        );
    }
}