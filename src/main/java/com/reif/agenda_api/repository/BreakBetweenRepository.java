package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.BreakBetween;
import com.reif.agenda_api.model.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BreakBetweenRepository extends JpaRepository<BreakBetween, Long> {

    List<BreakBetween> findByProfessional(Professional professional);

    List<BreakBetween> findByProfessionalId(Long professionalId);

    Optional<BreakBetween> findFirstByProfessionalId(Long professionalId);
    
}