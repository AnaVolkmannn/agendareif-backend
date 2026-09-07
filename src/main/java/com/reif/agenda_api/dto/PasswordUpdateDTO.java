package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateDTO(
        @NotBlank(message = "A nova senha é obrigatória")
        String newPassword
) {
}