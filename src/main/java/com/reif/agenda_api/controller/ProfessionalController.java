package com.reif.agenda_api.controller;

import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.service.ProfessionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @PostMapping
    public ResponseEntity<Professional> register(@Valid @RequestBody Professional professional) {
        Professional saved = professionalService.register(professional);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professional> findById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Professional>> findAll() {
        return ResponseEntity.ok(professionalService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professional> update(@PathVariable Long id,
                                                @Valid @RequestBody Professional professional) {
        return ResponseEntity.ok(professionalService.update(id, professional));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        professionalService.updatePassword(id, body.get("password"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}