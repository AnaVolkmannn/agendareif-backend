package com.reif.agenda_api.service;

import com.reif.agenda_api.dto.BreakBetweenRequestDTO;
import com.reif.agenda_api.dto.BreakBetweenResponseDTO;
import com.reif.agenda_api.model.BreakBetween;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.repository.BreakBetweenRepository;
import com.reif.agenda_api.repository.ProfessionalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BreakBetweenService {

    private final BreakBetweenRepository breakBetweenRepository;
    private final ProfessionalRepository professionalRepository;

    public BreakBetweenService(BreakBetweenRepository breakBetweenRepository,
                                ProfessionalRepository professionalRepository) {
        this.breakBetweenRepository = breakBetweenRepository;
        this.professionalRepository = professionalRepository;
    }

    public BreakBetweenResponseDTO findByProfessionalId(Long professionalId) {
        return BreakBetweenResponseDTO.fromEntity(findEntityByProfessionalId(professionalId));
    }

    public BreakBetweenResponseDTO create(BreakBetweenRequestDTO dto) {
        if (breakBetweenRepository.existsByProfessionalId(dto.professionalId())) {
            throw new IllegalArgumentException(
                    "Esse profissional já possui um descanso entre atendimentos cadastrado. Utilize o update.");
        }

        Professional professional = professionalRepository.findById(dto.professionalId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Professional não encontrado com id: " + dto.professionalId()));

        BreakBetween breakBetween = new BreakBetween();
        breakBetween.setProfessional(professional);
        breakBetween.setBreak_duration(dto.breakDuration());

        return BreakBetweenResponseDTO.fromEntity(breakBetweenRepository.save(breakBetween));
    }

    public BreakBetweenResponseDTO updateByProfessionalId(Long professionalId, BreakBetweenRequestDTO dto) {
        BreakBetween breakBetween = findEntityByProfessionalId(professionalId);
        breakBetween.setBreak_duration(dto.breakDuration());
        return BreakBetweenResponseDTO.fromEntity(breakBetweenRepository.save(breakBetween));
    }

    public void deleteByProfessionalId(Long professionalId) {
        BreakBetween breakBetween = findEntityByProfessionalId(professionalId);
        breakBetweenRepository.delete(breakBetween);
    }

    private BreakBetween findEntityByProfessionalId(Long professionalId) {
        return breakBetweenRepository.findByProfessionalId(professionalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "BreakBetween não encontrado para o profissional: " + professionalId));
    }
}