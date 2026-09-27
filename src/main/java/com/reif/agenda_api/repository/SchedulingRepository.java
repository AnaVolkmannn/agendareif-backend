package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.Scheduling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SchedulingRepository extends JpaRepository<Scheduling, Long> {

    List<Scheduling> findByClientId(Long clientId);

    List<Scheduling> findByProfessionalId(Long professionalId);

    List<Scheduling> findByProfessionalIdAndScheduledAtBetween(
            Long professionalId, LocalDateTime start, LocalDateTime end);
    
    List<Scheduling> findByCanceledFalseAndReminderSentFalseAndScheduledAtBetween(
    LocalDateTime start, LocalDateTime end);

    List<Scheduling> findByCanceledFalse();

    Optional<Scheduling> findByCancellationToken(String cancellationToken);

}