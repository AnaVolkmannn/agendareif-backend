package com.reif.agenda_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.reif.agenda_api.model.PauseWeeklySchedule;

import java.time.LocalTime;

public record PauseWeeklyScheduleResponseDTO(
        Long id,
        Long weeklyScheduleId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime endTime
) {

    public static PauseWeeklyScheduleResponseDTO fromEntity(PauseWeeklySchedule pause) {
        return new PauseWeeklyScheduleResponseDTO(
                pause.getId(),
                pause.getWeeklySchedule().getId(),
                pause.getStartTime(),
                pause.getEndTime()
        );
    }
}
