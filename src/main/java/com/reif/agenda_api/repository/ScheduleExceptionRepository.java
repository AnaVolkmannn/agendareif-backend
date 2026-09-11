package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long> {

    List<ScheduleException> findByProfessionalId(Long professionalId);

    boolean existsByProfessionalIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long professionalId, LocalDateTime endTime, LocalDateTime startTime
    );
}