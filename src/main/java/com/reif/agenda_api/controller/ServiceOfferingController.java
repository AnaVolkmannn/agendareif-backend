package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.ServiceOfferingRequestDTO;
import com.reif.agenda_api.dto.ServiceOfferingResponseDTO;
import com.reif.agenda_api.model.ServiceOffering;
import com.reif.agenda_api.service.ServiceOfferingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professionals/{professionalId}/services")
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    public ServiceOfferingController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    @PostMapping
    public ResponseEntity<ServiceOfferingResponseDTO> create(@PathVariable Long professionalId,
                                                               @Valid @RequestBody ServiceOfferingRequestDTO request) {
        ServiceOffering saved = serviceOfferingService.create(professionalId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceOfferingResponseDTO.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<ServiceOfferingResponseDTO>> findAll(@PathVariable Long professionalId) {
        List<ServiceOfferingResponseDTO> response = serviceOfferingService.findAllByProfessional(professionalId).stream()
                .map(ServiceOfferingResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOfferingResponseDTO> findOne(@PathVariable Long professionalId,
                                                                @PathVariable Long id) {
        ServiceOffering service = serviceOfferingService.findOne(professionalId, id);
        return ResponseEntity.ok(ServiceOfferingResponseDTO.fromEntity(service));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOfferingResponseDTO> update(@PathVariable Long professionalId,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody ServiceOfferingRequestDTO request) {
        ServiceOffering updated = serviceOfferingService.update(professionalId, id, request);
        return ResponseEntity.ok(ServiceOfferingResponseDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long professionalId, @PathVariable Long id) {
        serviceOfferingService.delete(professionalId, id);
        return ResponseEntity.noContent().build();
    }
}
