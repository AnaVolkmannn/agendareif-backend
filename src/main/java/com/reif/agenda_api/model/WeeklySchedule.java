package com.reif.agenda_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Horário padrão de trabalho do profissional em um dia da semana.
 * Cada profissional tem no máximo uma linha por dia da semana.
 */
@Entity
@Table(
        name = "weekly_schedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_weekly_schedule_professional_day",
                columnNames = {"professional_id", "day_of_week"}
        )
)
@NoArgsConstructor
public class WeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    /** 0 = domingo … 6 = sábado. */
    @NotNull
    @Min(0)
    @Max(6)
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    /**
     * false = dia fechado, não aceita agendamento.
     * Começa fechado por padrão (RF18 — a agenda é fechada até o
     * profissional liberar o horário).
     */
    @Column(nullable = false)
    private boolean active = false;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    /** Minutos de descanso entre um cliente e outro. 0 = sem descanso. */
    @Column(name = "break_between", nullable = false)
    private Integer breakBetween = 0;

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

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getBreakBetween() {
        return breakBetween;
    }

    public void setBreakBetween(Integer breakBetween) {
        this.breakBetween = breakBetween;
    }
}
