package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.PauseException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PauseExceptionRepository extends JpaRepository<PauseException, Long> {

    List<PauseException> findByScheduleExceptionId(Long scheduleExceptionId);
}