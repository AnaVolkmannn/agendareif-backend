package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.ScheduleExceptionRequestDTO;
import com.reif.agenda_api.dto.ScheduleExceptionResponseDTO;
import com.reif.agenda_api.model.ScheduleException;
import com.reif.agenda_api.service.ScheduleExceptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professionals/{professionalId}/schedule-exceptions")
public class ScheduleExceptionController {

    private final ScheduleExceptionService scheduleExceptionService;

    public ScheduleExceptionController(ScheduleExceptionService scheduleExceptionService) {
        this.scheduleExceptionService = scheduleExceptionService;
    }

    @PostMapping
    public ResponseEntity<ScheduleExceptionResponseDTO> create(@PathVariable Long professionalId,
                                                                @Valid @RequestBody ScheduleExceptionRequestDTO request) {
        ScheduleException exception = new ScheduleException();
        exception.setType(request.type());
        exception.setStartTime(request.startTime());
        exception.setEndTime(request.endTime());

        ScheduleException saved = scheduleExceptionService.create(professionalId, exception);
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleExceptionResponseDTO.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleExceptionResponseDTO>> findByProfessional(@PathVariable Long professionalId) {
        List<ScheduleExceptionResponseDTO> response = scheduleExceptionService.findByProfessional(professionalId).stream()
                .map(ScheduleExceptionResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long professionalId, @PathVariable Long id) {
        scheduleExceptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}