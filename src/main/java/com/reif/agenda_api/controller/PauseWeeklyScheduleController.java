package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.PauseWeeklyScheduleRequestDTO;
import com.reif.agenda_api.dto.PauseWeeklyScheduleResponseDTO;
import com.reif.agenda_api.model.PauseWeeklySchedule;
import com.reif.agenda_api.service.PauseWeeklyScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/weekly-schedules/{weeklyScheduleId}/pauses")
public class PauseWeeklyScheduleController {

    private final PauseWeeklyScheduleService pauseWeeklyScheduleService;

    public PauseWeeklyScheduleController(PauseWeeklyScheduleService pauseWeeklyScheduleService) {
        this.pauseWeeklyScheduleService = pauseWeeklyScheduleService;
    }

    @PostMapping
    public ResponseEntity<PauseWeeklyScheduleResponseDTO> create(@PathVariable Long weeklyScheduleId,
                                                                  @Valid @RequestBody PauseWeeklyScheduleRequestDTO request) {
        PauseWeeklySchedule pause = new PauseWeeklySchedule();
        pause.setStartTime(request.startTime());
        pause.setEndTime(request.endTime());

        PauseWeeklySchedule saved = pauseWeeklyScheduleService.create(weeklyScheduleId, pause);
        return ResponseEntity.status(HttpStatus.CREATED).body(PauseWeeklyScheduleResponseDTO.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<PauseWeeklyScheduleResponseDTO>> findByWeeklySchedule(@PathVariable Long weeklyScheduleId) {
        List<PauseWeeklyScheduleResponseDTO> response =
                pauseWeeklyScheduleService.findByWeeklySchedule(weeklyScheduleId).stream()
                        .map(PauseWeeklyScheduleResponseDTO::fromEntity)
                        .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long weeklyScheduleId, @PathVariable Long id) {
        pauseWeeklyScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
