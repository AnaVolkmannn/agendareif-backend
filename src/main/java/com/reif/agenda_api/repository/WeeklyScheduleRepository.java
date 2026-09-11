package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.WeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {

    List<WeeklySchedule> findByProfessionalIdOrderByDayOfWeekAsc(Long professionalId);

    Optional<WeeklySchedule> findByIdAndProfessionalId(Long id, Long professionalId);

    Optional<WeeklySchedule> findByProfessionalIdAndDayOfWeek(Long professionalId, Integer dayOfWeek);

    boolean existsByProfessionalIdAndDayOfWeek(Long professionalId, Integer dayOfWeek);
}
