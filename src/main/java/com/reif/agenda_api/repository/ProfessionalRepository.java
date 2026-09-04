package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    Optional<Professional> findByEmail(String email);

    boolean existsByEmail(String email);
}