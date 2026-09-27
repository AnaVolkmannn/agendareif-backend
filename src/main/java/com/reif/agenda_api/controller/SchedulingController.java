package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.SchedulingRequestDTO;
import com.reif.agenda_api.dto.SchedulingResponseDTO;
import com.reif.agenda_api.model.Scheduling;
import com.reif.agenda_api.service.AvailabilityService;
import com.reif.agenda_api.service.SchedulingService;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedulings")
public class SchedulingController {

    private final SchedulingService schedulingService;
    private final AvailabilityService availabilityService;

    public SchedulingController(SchedulingService schedulingService, AvailabilityService availabilityService) {
        this.schedulingService = schedulingService;
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<SchedulingResponseDTO> create(@Valid @RequestBody SchedulingRequestDTO dto) {
        Scheduling scheduling = schedulingService.create(
                dto.getClientId(), dto.getProfessionalId(), dto.getServiceId(), dto.getScheduledAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(new SchedulingResponseDTO(scheduling));
    }

    @GetMapping
    public ResponseEntity<List<SchedulingResponseDTO>> findAll() {
        List<SchedulingResponseDTO> result = schedulingService.findAll().stream()
                .map(SchedulingResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchedulingResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new SchedulingResponseDTO(schedulingService.findById(id)));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<SchedulingResponseDTO>> findByClient(@PathVariable Long clientId) {
        List<SchedulingResponseDTO> result = schedulingService.findByClient(clientId).stream()
                .map(SchedulingResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<SchedulingResponseDTO>> findByProfessional(@PathVariable Long professionalId) {
        List<SchedulingResponseDTO> result = schedulingService.findByProfessional(professionalId).stream()
                .map(SchedulingResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SchedulingResponseDTO>> findActive() {
        List<SchedulingResponseDTO> result = schedulingService.findActive().stream()
                .map(SchedulingResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<List<LocalDateTime>> getAvailableSlots(
        @RequestParam Long professionalId,
        @RequestParam Long serviceId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(availabilityService.getAvailableSlots(professionalId, serviceId, date));
}

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        schedulingService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cancel/token/{token}")
    public ResponseEntity<Void> cancelByToken(@PathVariable String token) {
        schedulingService.cancelByToken(token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        schedulingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}