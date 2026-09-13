package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByProfessionalIdOrderBySortOrderAsc(Long professionalId);

    Optional<Portfolio> findByIdAndProfessionalId(Long id, Long professionalId);

    @Query("SELECT COALESCE(MAX(p.sortOrder), -1) FROM Portfolio p WHERE p.professional.id = :professionalId")
    Integer findMaxSortOrderByProfessionalId(@Param("professionalId") Long professionalId);
}