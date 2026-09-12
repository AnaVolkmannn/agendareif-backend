package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.AdministratorRequestDTO;
import com.reif.agenda_api.dto.AdministratorResponseDTO;
import com.reif.agenda_api.dto.PasswordUpdateDTO;
import com.reif.agenda_api.model.Administrator;
import com.reif.agenda_api.service.AdministratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/administrators")
public class AdministratorController {

    private final AdministratorService administratorService;

    public AdministratorController(AdministratorService administratorService) {
        this.administratorService = administratorService;
    }

    @PostMapping
    public ResponseEntity<AdministratorResponseDTO> register(@Valid @RequestBody AdministratorRequestDTO request) {
        Administrator administrator = new Administrator();
        administrator.setEmail(request.email());
        administrator.setPassword(request.password());

        Administrator saved = administratorService.register(administrator);
        return ResponseEntity.status(HttpStatus.CREATED).body(AdministratorResponseDTO.fromEntity(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministratorResponseDTO> findById(@PathVariable Long id) {
        Administrator administrator = administratorService.findById(id);
        return ResponseEntity.ok(AdministratorResponseDTO.fromEntity(administrator));
    }

    @GetMapping
    public ResponseEntity<List<AdministratorResponseDTO>> findAll() {
        List<AdministratorResponseDTO> response = administratorService.findAll().stream()
                .map(AdministratorResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                                @Valid @RequestBody PasswordUpdateDTO request) {
        administratorService.updatePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        administratorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
