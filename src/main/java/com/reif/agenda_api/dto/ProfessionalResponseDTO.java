package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.Professional;

import java.time.LocalDateTime;

public record ProfessionalResponseDTO(
        Long id,
        String name,
        String email,
        String phone,
        String profilePicture,
        String description,
        Professional.ScheduleMode scheduleMode,
        LocalDateTime createdAt
) {

    public static ProfessionalResponseDTO fromEntity(Professional professional) {
        return new ProfessionalResponseDTO(
                professional.getId(),
                professional.getName(),
                professional.getEmail(),
                professional.getPhone(),
                professional.getProfilePicture(),
                professional.getDescription(),
                professional.getScheduleMode(),
                professional.getCreatedAt()
        );
    }
}