package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.PauseExceptionRequestDTO;
import com.reif.agenda_api.dto.PauseExceptionResponseDTO;
import com.reif.agenda_api.model.PauseException;
import com.reif.agenda_api.service.PauseExceptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule-exceptions/{scheduleExceptionId}/pauses")
public class PauseExceptionController {

    private final PauseExceptionService pauseExceptionService;

    public PauseExceptionController(PauseExceptionService pauseExceptionService) {
        this.pauseExceptionService = pauseExceptionService;
    }

    @PostMapping
    public ResponseEntity<PauseExceptionResponseDTO> create(@PathVariable Long scheduleExceptionId,
                                                             @Valid @RequestBody PauseExceptionRequestDTO request) {
        PauseException pause = new PauseException();
        pause.setStartTime(request.startTime());
        pause.setEndTime(request.endTime());

        PauseException saved = pauseExceptionService.create(scheduleExceptionId, pause);
        return ResponseEntity.status(HttpStatus.CREATED).body(PauseExceptionResponseDTO.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<PauseExceptionResponseDTO>> findByScheduleException(@PathVariable Long scheduleExceptionId) {
        List<PauseExceptionResponseDTO> response = pauseExceptionService.findByScheduleException(scheduleExceptionId).stream()
                .map(PauseExceptionResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long scheduleExceptionId, @PathVariable Long id) {
        pauseExceptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}