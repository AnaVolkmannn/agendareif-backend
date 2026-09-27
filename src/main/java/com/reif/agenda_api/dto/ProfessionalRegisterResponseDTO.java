package com.reif.agenda_api.dto;

public record ProfessionalRegisterResponseDTO(
        ProfessionalResponseDTO professional,
        String temporaryPassword
) {
}