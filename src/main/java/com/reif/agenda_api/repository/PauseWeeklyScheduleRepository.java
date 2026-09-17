package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.PauseWeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PauseWeeklyScheduleRepository extends JpaRepository<PauseWeeklySchedule, Long> {

    List<PauseWeeklySchedule> findByWeeklyScheduleIdOrderByStartTimeAsc(Long weeklyScheduleId);
}
