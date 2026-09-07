package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.Professional.ScheduleMode;

import jakarta.validation.constraints.NotBlank;

public record ProfessionalUpdateDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @NotBlank(message = "O telefone é obrigatório")
        String phone,
        String profilePicture,
        ScheduleMode scheduleMode,
        String description
) {
}