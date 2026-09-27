package com.reif.agenda_api.config;

import com.reif.agenda_api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize nos controllers
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // docs
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // login é sempre público
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                // fluxo do cliente (sem login)
                .requestMatchers(HttpMethod.GET, "/schedulings/available-slots").permitAll()
                .requestMatchers(HttpMethod.POST, "/schedulings").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/schedulings/cancel/token/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/professionals/**").permitAll() // cliente navega os profissionais pra escolher

                // só admin
                .requestMatchers(HttpMethod.GET, "/schedulings").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/schedulings/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/professionals").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/administrators/**").hasRole("ADMIN")

                // admin ou profissional (checagem de "é dono?" fica em @PreAuthorize no endpoint)
                .requestMatchers("/schedulings/professional/**").hasAnyRole("ADMIN", "PROFESSIONAL")
                .requestMatchers("/weekly-schedules/**").hasAnyRole("ADMIN", "PROFESSIONAL")
                .requestMatchers("/schedule-exceptions/**").hasAnyRole("ADMIN", "PROFESSIONAL")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}