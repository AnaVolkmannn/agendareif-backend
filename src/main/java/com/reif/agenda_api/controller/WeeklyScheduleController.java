package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.WeeklyScheduleBatchDTO;
import com.reif.agenda_api.dto.WeeklyScheduleRequestDTO;
import com.reif.agenda_api.dto.WeeklyScheduleResponseDTO;
import com.reif.agenda_api.model.WeeklySchedule;
import com.reif.agenda_api.service.WeeklyScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professionals/{professionalId}/weekly-schedules")
public class WeeklyScheduleController {

    private final WeeklyScheduleService weeklyScheduleService;

    public WeeklyScheduleController(WeeklyScheduleService weeklyScheduleService) {
        this.weeklyScheduleService = weeklyScheduleService;
    }

    @PostMapping
    public ResponseEntity<WeeklyScheduleResponseDTO> create(@PathVariable Long professionalId,
                                                             @Valid @RequestBody WeeklyScheduleRequestDTO request) {
        WeeklySchedule saved = weeklyScheduleService.create(professionalId, toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(WeeklyScheduleResponseDTO.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<WeeklyScheduleResponseDTO>> findAll(@PathVariable Long professionalId) {
        List<WeeklyScheduleResponseDTO> response = weeklyScheduleService.findAllByProfessional(professionalId).stream()
                .map(WeeklyScheduleResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WeeklyScheduleResponseDTO> findById(@PathVariable Long professionalId,
                                                               @PathVariable Long id) {
        WeeklySchedule weeklySchedule = weeklyScheduleService.findById(professionalId, id);
        return ResponseEntity.ok(WeeklyScheduleResponseDTO.fromEntity(weeklySchedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WeeklyScheduleResponseDTO> update(@PathVariable Long professionalId,
                                                             @PathVariable Long id,
                                                             @Valid @RequestBody WeeklyScheduleRequestDTO request) {
        WeeklySchedule updated = weeklyScheduleService.update(professionalId, id, toEntity(request));
        return ResponseEntity.ok(WeeklyScheduleResponseDTO.fromEntity(updated));
    }

    /** Cadastro em lote da semana inteira (RF24). */
    @PutMapping
    public ResponseEntity<List<WeeklyScheduleResponseDTO>> replaceWeek(@PathVariable Long professionalId,
                                                                        @Valid @RequestBody WeeklyScheduleBatchDTO request) {
        List<WeeklySchedule> days = request.days().stream()
                .map(WeeklyScheduleController::toEntity)
                .toList();

        List<WeeklyScheduleResponseDTO> response = weeklyScheduleService.replaceWeek(professionalId, days).stream()
                .map(WeeklyScheduleResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long professionalId, @PathVariable Long id) {
        weeklyScheduleService.delete(professionalId, id);
        return ResponseEntity.noContent().build();
    }

    private static WeeklySchedule toEntity(WeeklyScheduleRequestDTO request) {
        WeeklySchedule weeklySchedule = new WeeklySchedule();
        weeklySchedule.setDayOfWeek(request.dayOfWeek());
        // Sem valor explícito o dia nasce fechado (RF18).
        weeklySchedule.setActive(Boolean.TRUE.equals(request.active()));
        weeklySchedule.setStartTime(request.startTime());
        weeklySchedule.setEndTime(request.endTime());
        return weeklySchedule;
    }
}
