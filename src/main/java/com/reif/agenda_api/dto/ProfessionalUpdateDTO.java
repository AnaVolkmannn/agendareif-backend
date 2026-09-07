package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.Professional.ScheduleMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfessionalUpdateDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @NotBlank(message = "O telefone é obrigatório")
        String phone,
        @Email @NotBlank(message = "O e-mail é obrigatório")
        String email,
        String profilePicture,
        ScheduleMode scheduleMode,
        String description
) {
}