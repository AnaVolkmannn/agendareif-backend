package com.reif.agenda_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "break_between")
@NoArgsConstructor
public class BreakBetween {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @NotBlank
    @Column(nullable = false)
    private Integer break_duration;

    // --- Getters e setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }
    
    public Integer getBreak_duration() {
        return break_duration;
    }

    public void setBreak_duration(Integer break_duration) {
        this.break_duration = break_duration;
    }

}