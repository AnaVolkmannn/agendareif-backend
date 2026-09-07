package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.repository.ProfessionalRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessionalService(ProfessionalRepository professionalRepository,
                                PasswordEncoder passwordEncoder) {
        this.professionalRepository = professionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Professional register(Professional professional) {
        if (professionalRepository.existsByEmail(professional.getEmail())) {
            throw new IllegalArgumentException("Já existe um profissional cadastrado com esse e-mail.");
        }
        professional.setPassword(passwordEncoder.encode(professional.getPassword()));
        return professionalRepository.save(professional);
    }

    public Professional findById(Long id) {
        return professionalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));
    }

    public Professional findByEmail(String email) {
        return professionalRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));
    }

    public List<Professional> findAll() {
        return professionalRepository.findAll();
    }

    @Transactional
    public Professional update(Long id, Professional data) {
        Professional professional = findById(id);
        professional.setName(data.getName());
        professional.setPhone(data.getPhone());
        professional.setProfilePicture(data.getProfilePicture());
        professional.setDescription(data.getDescription());
        professional.setScheduleMode(data.getScheduleMode());
        return professionalRepository.save(professional);
    }

    @Transactional
    public void updatePassword(Long id, String newRawPassword) {
        Professional professional = findById(id);
        professional.setPassword(passwordEncoder.encode(newRawPassword));
        professionalRepository.save(professional);
    }

    @Transactional
    public void delete(Long id) {
        Professional professional = findById(id);
        professionalRepository.delete(professional);
    }
}