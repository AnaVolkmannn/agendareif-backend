package com.reif.agenda_api.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Intervalo dentro de um dia do horário semanal (almoço, café etc).
 * O horário é só hora, sem data, porque o dia da semana se repete.
 */
@Entity
@Table(name = "pause_weekly_schedule")
@NoArgsConstructor
public class PauseWeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_schedule_id", nullable = false)
    private WeeklySchedule weeklySchedule;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // --- Getters e setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WeeklySchedule getWeeklySchedule() {
        return weeklySchedule;
    }

    public void setWeeklySchedule(WeeklySchedule weeklySchedule) {
        this.weeklySchedule = weeklySchedule;
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
}
