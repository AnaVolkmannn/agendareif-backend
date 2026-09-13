package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.BreakBetweenRequestDTO;
import com.reif.agenda_api.dto.BreakBetweenResponseDTO;
import com.reif.agenda_api.service.BreakBetweenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/break-between")
public class BreakBetweenController {

    private final BreakBetweenService breakBetweenService;

    public BreakBetweenController(BreakBetweenService breakBetweenService) {
        this.breakBetweenService = breakBetweenService;
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<BreakBetweenResponseDTO> findByProfessionalId(@PathVariable Long professionalId) {
        return ResponseEntity.ok(breakBetweenService.findByProfessionalId(professionalId));
    }

    @PostMapping
    public ResponseEntity<BreakBetweenResponseDTO> create(@Valid @RequestBody BreakBetweenRequestDTO dto) {
        BreakBetweenResponseDTO created = breakBetweenService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/professional/{professionalId}")
    public ResponseEntity<BreakBetweenResponseDTO> update(@PathVariable Long professionalId,
                                                           @Valid @RequestBody BreakBetweenRequestDTO dto) {
        return ResponseEntity.ok(breakBetweenService.updateByProfessionalId(professionalId, dto));
    }

    @DeleteMapping("/professional/{professionalId}")
    public ResponseEntity<Void> delete(@PathVariable Long professionalId) {
        breakBetweenService.deleteByProfessionalId(professionalId);
        return ResponseEntity.noContent().build();
    }
}