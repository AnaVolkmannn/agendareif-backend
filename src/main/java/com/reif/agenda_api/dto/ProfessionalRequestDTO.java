package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.Professional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfessionalRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String password,

        @NotBlank(message = "O telefone é obrigatório")
        String phone,

        String profilePicture,

        Professional.ScheduleMode scheduleMode,
        
        String description
) {
}