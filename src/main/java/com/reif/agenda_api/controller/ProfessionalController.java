package com.reif.agenda_api.controller;

import com.reif.agenda_api.dto.*;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.service.ProfessionalRegistrationResult;
import com.reif.agenda_api.service.ProfessionalService;
import com.reif.agenda_api.security.JwtService;
import com.reif.agenda_api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;
    private final JwtService jwtService;

    public ProfessionalController(ProfessionalService professionalService, JwtService jwtService) {
        this.professionalService = professionalService;
        this.jwtService = jwtService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalRegisterResponseDTO> register(@Valid @RequestBody ProfessionalRequestDTO request) {
        Professional professional = new Professional();
        professional.setName(request.name());
        professional.setEmail(request.email());
        professional.setPhone(request.phone());
        professional.setProfilePicture(request.profilePicture());
        professional.setDescription(request.description());
        professional.setScheduleMode(
                request.scheduleMode() != null ? request.scheduleMode() : Professional.ScheduleMode.ONLINE
        );

        ProfessionalRegistrationResult result = professionalService.register(professional);

        ProfessionalRegisterResponseDTO response = new ProfessionalRegisterResponseDTO(
                ProfessionalResponseDTO.fromEntity(result.professional()),
                result.temporaryPassword()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponseDTO> findById(@PathVariable Long id) {
        Professional professional = professionalService.findById(id);
        return ResponseEntity.ok(ProfessionalResponseDTO.fromEntity(professional));
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponseDTO>> findAll() {
        List<ProfessionalResponseDTO> response = professionalService.findAll().stream()
                .map(ProfessionalResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESSIONAL') and #id == authentication.principal.id)")
    public ResponseEntity<ProfessionalResponseDTO> update(@PathVariable("id") Long id,
                                                           @Valid @RequestBody ProfessionalUpdateDTO request) {
        Professional data = new Professional();
        data.setName(request.name());
        data.setPhone(request.phone());
        data.setEmail(request.email());
        data.setProfilePicture(request.profilePicture());
        data.setDescription(request.description());
        data.setScheduleMode(request.scheduleMode());

        Professional updated = professionalService.update(id, data);
        return ResponseEntity.ok(ProfessionalResponseDTO.fromEntity(updated));
    }

    /** Reset feito pelo admin — não exige senha atual. */
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                                @Valid @RequestBody PasswordUpdateDTO request) {
        professionalService.updatePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /** Troca feita pelo próprio profissional — exige senha atual, usado no primeiro acesso e depois. */
    @PatchMapping("/{id}/change-password")
    @PreAuthorize("hasRole('PROFESSIONAL') and #id == authentication.principal.id")
    public ResponseEntity<LoginResponseDTO> changePassword(@PathVariable("id") Long id,
                                                        @Valid @RequestBody ChangePasswordRequestDTO request) {
        Professional p = professionalService.changePassword(id, request.oldPassword(), request.newPassword());

        AuthenticatedUser user = new AuthenticatedUser(
                p.getId(), p.getEmail(), null, "PROFESSIONAL", false);

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new LoginResponseDTO(
                token, user.getRole(), user.getId(), user.getUsername(), user.isMustChangePassword()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}