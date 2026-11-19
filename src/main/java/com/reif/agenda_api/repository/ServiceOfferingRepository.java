package com.reif.agenda_api.repository;

import com.reif.agenda_api.model.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    List<ServiceOffering> findByProfessionalId(Long professionalId);

    /** Lista na ordem de cadastro (id crescente), mesmo depois de editar. */
    List<ServiceOffering> findByProfessionalIdOrderByIdAsc(Long professionalId);

    Optional<ServiceOffering> findByIdAndProfessionalId(Long id, Long professionalId);
}