package com.reif.agenda_api.service;

import com.reif.agenda_api.dto.BreakBetweenRequestDTO;
import com.reif.agenda_api.dto.BreakBetweenResponseDTO;
import com.reif.agenda_api.model.BreakBetween;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.repository.BreakBetweenRepository;
import com.reif.agenda_api.repository.ProfessionalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BreakBetweenService {

    private final BreakBetweenRepository breakBetweenRepository;
    private final ProfessionalRepository professionalRepository;

    public BreakBetweenService(BreakBetweenRepository breakBetweenRepository,
                                ProfessionalRepository professionalRepository) {
        this.breakBetweenRepository = breakBetweenRepository;
        this.professionalRepository = professionalRepository;
    }

    public List<BreakBetweenResponseDTO> findAll() {
        return breakBetweenRepository.findAll()
                .stream()
                .map(BreakBetweenResponseDTO::fromEntity)
                .toList();
    }

    public BreakBetweenResponseDTO findById(Long id) {
        return BreakBetweenResponseDTO.fromEntity(findEntityById(id));
    }

    public List<BreakBetweenResponseDTO> findByProfessionalId(Long professionalId) {
        return breakBetweenRepository.findByProfessionalId(professionalId)
                .stream()
                .map(BreakBetweenResponseDTO::fromEntity)
                .toList();
    }

    public BreakBetweenResponseDTO create(BreakBetweenRequestDTO dto) {
        Professional professional = professionalRepository.findById(dto.professionalId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Professional não encontrado com id: " + dto.professionalId()));

        BreakBetween breakBetween = new BreakBetween();
        breakBetween.setProfessional(professional);
        breakBetween.setBreak_duration(dto.breakDuration());

        return BreakBetweenResponseDTO.fromEntity(breakBetweenRepository.save(breakBetween));
    }

    public BreakBetweenResponseDTO update(Long id, BreakBetweenRequestDTO dto) {
        BreakBetween breakBetween = findEntityById(id);

        if (!breakBetween.getProfessional().getId().equals(dto.professionalId())) {
            Professional professional = professionalRepository.findById(dto.professionalId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Professional não encontrado com id: " + dto.professionalId()));
            breakBetween.setProfessional(professional);
        }

        breakBetween.setBreak_duration(dto.breakDuration());

        return BreakBetweenResponseDTO.fromEntity(breakBetweenRepository.save(breakBetween));
    }

    public void delete(Long id) {
        BreakBetween breakBetween = findEntityById(id);
        breakBetweenRepository.delete(breakBetween);
    }

    private BreakBetween findEntityById(Long id) {
        return breakBetweenRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BreakBetween não encontrado com id: " + id));
    }
}