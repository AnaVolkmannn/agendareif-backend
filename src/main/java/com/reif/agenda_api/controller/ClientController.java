package com.reif.agenda_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reif.agenda_api.dto.ClientRequestDTO;
import com.reif.agenda_api.dto.ClientResponseDTO;
import com.reif.agenda_api.model.Client;
import com.reif.agenda_api.service.ClientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientRequestDTO request) {
        Client saved = clientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClientResponseDTO.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> findAll() {
        List<ClientResponseDTO> response = clientService.findAll().stream()
                .map(ClientResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> findOne(@PathVariable Long id) {
        Client client = clientService.findOne(id);
        return ResponseEntity.ok(ClientResponseDTO.fromEntity(client));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody ClientRequestDTO request) {
        Client updated = clientService.update(id, request);
        return ResponseEntity.ok(ClientResponseDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}