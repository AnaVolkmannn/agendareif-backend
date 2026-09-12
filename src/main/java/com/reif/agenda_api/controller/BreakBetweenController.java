package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.BreakBetweenRequestDTO;
import com.reif.agenda_api.dto.BreakBetweenResponseDTO;
import com.reif.agenda_api.service.BreakBetweenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/break-between")
public class BreakBetweenController {

    private final BreakBetweenService breakBetweenService;

    public BreakBetweenController(BreakBetweenService breakBetweenService) {
        this.breakBetweenService = breakBetweenService;
    }

    @GetMapping
    public ResponseEntity<List<BreakBetweenResponseDTO>> findAll() {
        return ResponseEntity.ok(breakBetweenService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BreakBetweenResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(breakBetweenService.findById(id));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<BreakBetweenResponseDTO>> findByProfessionalId(@PathVariable Long professionalId) {
        return ResponseEntity.ok(breakBetweenService.findByProfessionalId(professionalId));
    }

    @PostMapping
    public ResponseEntity<BreakBetweenResponseDTO> create(@RequestBody BreakBetweenRequestDTO dto) {
        BreakBetweenResponseDTO created = breakBetweenService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BreakBetweenResponseDTO> update(@PathVariable Long id,
                                                           @Valid @RequestBody BreakBetweenRequestDTO dto) {
        return ResponseEntity.ok(breakBetweenService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        breakBetweenService.delete(id);
        return ResponseEntity.noContent().build();
    }
}