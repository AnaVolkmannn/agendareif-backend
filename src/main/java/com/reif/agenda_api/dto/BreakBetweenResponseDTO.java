package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.BreakBetween;

public record BreakBetweenResponseDTO(

        Long id,
        Long professionalId,
        Integer breakDuration

) {
    public static BreakBetweenResponseDTO fromEntity(BreakBetween breakBetween) {
        return new BreakBetweenResponseDTO(
                breakBetween.getId(),
                breakBetween.getProfessional().getId(),
                breakBetween.getBreak_duration()
        );
    }
}