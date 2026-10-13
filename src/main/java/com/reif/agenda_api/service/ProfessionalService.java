package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.repository.ProfessionalRepository;
import com.reif.agenda_api.security.TemporaryPasswordGenerator;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.authentication.BadCredentialsException;
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

    /**
     * Registro feito pelo admin. Gera uma senha temporária, salva com hash e
     * retorna o par (entidade salva, senha em texto puro) pra exibir uma única vez.
     */
    @Transactional
    public ProfessionalRegistrationResult register(Professional professional) {
        if (professionalRepository.existsByEmail(professional.getEmail())) {
            throw new IllegalArgumentException("Já existe um profissional cadastrado com esse e-mail.");
        }

        String rawPassword = TemporaryPasswordGenerator.generate();

        professional.setPassword(passwordEncoder.encode(rawPassword));
        professional.setMustChangePassword(true);

        Professional saved = professionalRepository.save(professional);

        return new ProfessionalRegistrationResult(saved, rawPassword);
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
        professional.setEmail(data.getEmail());
        professional.setProfilePicture(data.getProfilePicture());
        professional.setDescription(data.getDescription());
        professional.setScheduleMode(data.getScheduleMode());
        return professionalRepository.save(professional);
    }

    /** Usado pelo admin (ex: resetar senha esquecida) — não exige a senha atual. */
    @Transactional
    public void updatePassword(Long id, String newRawPassword) {
        Professional professional = findById(id);
        professional.setPassword(passwordEncoder.encode(newRawPassword));
        professionalRepository.save(professional);
    }

    /** Usado pelo próprio profissional (troca voluntária ou primeiro acesso) — exige a senha atual. */
    @Transactional
    public Professional changePassword(Long id, String oldPassword, String newPassword) {
        Professional p = professionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));

        if (!passwordEncoder.matches(oldPassword, p.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        p.setPassword(passwordEncoder.encode(newPassword));
        p.setMustChangePassword(false);
        return professionalRepository.save(p);
    }

    @Transactional
    public void delete(Long id) {
        Professional professional = findById(id);
        professionalRepository.delete(professional);
    }
}