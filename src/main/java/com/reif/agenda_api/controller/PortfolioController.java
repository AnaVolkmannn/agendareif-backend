package com.reif.agenda_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reif.agenda_api.dto.PortfolioBatchRequestDTO;
import com.reif.agenda_api.dto.PortfolioReorderRequestDTO;
import com.reif.agenda_api.dto.PortfolioRequestDTO;
import com.reif.agenda_api.dto.PortfolioResponseDTO;
import com.reif.agenda_api.model.Portfolio;
import com.reif.agenda_api.service.PortfolioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/professionals/{professionalId}/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<PortfolioResponseDTO> create(@PathVariable Long professionalId,
                                                        @Valid @RequestBody PortfolioRequestDTO request) {
        Portfolio saved = portfolioService.create(professionalId, request.imageUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioResponseDTO.fromEntity(saved));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<PortfolioResponseDTO>> createBatch(@PathVariable Long professionalId,
                                                                   @Valid @RequestBody PortfolioBatchRequestDTO request) {
        List<PortfolioResponseDTO> response = portfolioService.createBatch(professionalId, request.imageUrls()).stream()
                .map(PortfolioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponseDTO>> findAll(@PathVariable Long professionalId) {
        List<PortfolioResponseDTO> response = portfolioService.findAllByProfessional(professionalId).stream()
                .map(PortfolioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/reorder")
    public ResponseEntity<List<PortfolioResponseDTO>> reorder(@PathVariable Long professionalId,
                                                               @Valid @RequestBody PortfolioReorderRequestDTO request) {
        List<PortfolioResponseDTO> response = portfolioService.reorder(professionalId, request.orderedIds()).stream()
                .map(PortfolioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long professionalId, @PathVariable Long id) {
        portfolioService.delete(professionalId, id);
        return ResponseEntity.noContent().build();
    }
}