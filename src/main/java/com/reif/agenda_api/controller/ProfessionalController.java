package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.PasswordUpdateDTO;
import com.reif.agenda_api.dto.ProfessionalRequestDTO;
import com.reif.agenda_api.dto.ProfessionalResponseDTO;
import com.reif.agenda_api.dto.ProfessionalUpdateDTO;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.service.ProfessionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @PostMapping
    public ResponseEntity<ProfessionalResponseDTO> register(@Valid @RequestBody ProfessionalRequestDTO request) {
        Professional professional = new Professional();
        professional.setName(request.name());
        professional.setEmail(request.email());
        professional.setPassword(request.password());
        professional.setPhone(request.phone());
        professional.setProfilePicture(request.profilePicture());
        professional.setDescription(request.description());
        professional.setScheduleMode(
                request.scheduleMode() != null ? request.scheduleMode() : Professional.ScheduleMode.ONLINE
        );

        Professional saved = professionalService.register(professional);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfessionalResponseDTO.fromEntity(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponseDTO> findById(@PathVariable Long id) {
        Professional professional = professionalService.findById(id);
        return ResponseEntity.ok(ProfessionalResponseDTO.fromEntity(professional));
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponseDTO>> findAll() {
        List<ProfessionalResponseDTO> response = professionalService.findAll().stream()
                .map(ProfessionalResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalResponseDTO> update(@PathVariable Long id,
                                                           @Valid @RequestBody ProfessionalUpdateDTO request) {
        Professional data = new Professional();
        data.setName(request.name());
        data.setPhone(request.phone());
        data.setProfilePicture(request.profilePicture());
        data.setDescription(request.description());
        data.setScheduleMode(request.scheduleMode());

        Professional updated = professionalService.update(id, data);
        return ResponseEntity.ok(ProfessionalResponseDTO.fromEntity(updated));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                                @Valid @RequestBody PasswordUpdateDTO request) {
        professionalService.updatePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}