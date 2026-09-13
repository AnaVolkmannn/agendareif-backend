package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.BreakBetween;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BreakBetweenRepository extends JpaRepository<BreakBetween, Long> {

    Optional<BreakBetween> findByProfessionalId(Long professionalId);

    boolean existsByProfessionalId(Long professionalId);
    
}