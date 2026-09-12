package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Administrator;
import com.reif.agenda_api.repository.AdministratorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdministratorService {

    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministratorService(AdministratorRepository administratorRepository,
                                 PasswordEncoder passwordEncoder) {
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Administrator register(Administrator administrator) {
        if (administratorRepository.existsByEmail(administrator.getEmail())) {
            throw new IllegalArgumentException("Já existe um administrador cadastrado com esse e-mail.");
        }
        administrator.setPassword(passwordEncoder.encode(administrator.getPassword()));
        return administratorRepository.save(administrator);
    }

    public Administrator findById(Long id) {
        return administratorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador não encontrado."));
    }

    public Administrator findByEmail(String email) {
        return administratorRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Administrador não encontrado."));
    }

    public List<Administrator> findAll() {
        return administratorRepository.findAll();
    }

    @Transactional
    public void updatePassword(Long id, String newRawPassword) {
        Administrator administrator = findById(id);
        administrator.setPassword(passwordEncoder.encode(newRawPassword));
        administratorRepository.save(administrator);
    }

    @Transactional
    public void delete(Long id) {
        Administrator administrator = findById(id);
        administratorRepository.delete(administrator);
    }
}