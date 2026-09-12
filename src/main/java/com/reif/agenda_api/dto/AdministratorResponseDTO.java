package com.reif.agenda_api.dto;

import java.time.LocalDateTime;

import com.reif.agenda_api.model.Administrator;

public record AdministratorResponseDTO(
        Long id,
        String email,
        LocalDateTime createdAt
) {

    public static AdministratorResponseDTO fromEntity(Administrator administrator) {
        return new AdministratorResponseDTO(
                administrator.getId(),
                administrator.getEmail(),
                administrator.getCreatedAt()
        );
    }
}