package com.reif.agenda_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.reif.agenda_api.model.WeeklySchedule;

import java.time.LocalTime;

public record WeeklyScheduleResponseDTO(
        Long id,
        Long professionalId,
        Integer dayOfWeek,
        boolean active,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime endTime,

        Long breakBetweenId,
        Integer breakDuration
) {

    public static WeeklyScheduleResponseDTO fromEntity(WeeklySchedule weeklySchedule) {
        return new WeeklyScheduleResponseDTO(
                weeklySchedule.getId(),
                weeklySchedule.getProfessional().getId(),
                weeklySchedule.getDayOfWeek(),
                weeklySchedule.isActive(),
                weeklySchedule.getStartTime(),
                weeklySchedule.getEndTime(),
                weeklySchedule.getBreakBetween() != null ? weeklySchedule.getBreakBetween().getId() : null,
                weeklySchedule.getBreakBetween() != null ? weeklySchedule.getBreakBetween().getBreak_duration() : null
        );
    }
}