package com.reif.agenda_api.security;

import com.reif.agenda_api.model.Administrator;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.repository.AdministratorRepository;
import com.reif.agenda_api.repository.ProfessionalRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdministratorRepository administratorRepository;
    private final ProfessionalRepository professionalRepository;

    public CustomUserDetailsService(AdministratorRepository administratorRepository,
                                     ProfessionalRepository professionalRepository) {
        this.administratorRepository = administratorRepository;
        this.professionalRepository = professionalRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return administratorRepository.findByEmail(email)
                .map(this::toAuthenticatedUser)
                .or(() -> professionalRepository.findByEmail(email).map(this::toAuthenticatedUser))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    private AuthenticatedUser toAuthenticatedUser(Administrator admin) {
        return new AuthenticatedUser(admin.getId(), admin.getEmail(), admin.getPassword(), "ADMIN", false);
    }

    private AuthenticatedUser toAuthenticatedUser(Professional professional) {
        return new AuthenticatedUser(
                professional.getId(),
                professional.getEmail(),
                professional.getPassword(),
                "PROFESSIONAL",
                professional.isMustChangePassword()
        );
    }
}