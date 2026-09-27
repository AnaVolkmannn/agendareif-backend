package com.reif.agenda_api.config;

import com.reif.agenda_api.model.Administrator;
import com.reif.agenda_api.repository.AdministratorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeedConfig {

    @Bean
    public CommandLineRunner seedFirstAdmin(AdministratorRepository administratorRepository,
                                             PasswordEncoder passwordEncoder) {
        return args -> {
            if (administratorRepository.count() == 0) {
                Administrator admin = new Administrator();
                admin.setEmail("admin@reif.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                administratorRepository.save(admin);
                System.out.println(">>> Admin inicial criado: admin@reif.com / admin123");
            }
        };
    }
}