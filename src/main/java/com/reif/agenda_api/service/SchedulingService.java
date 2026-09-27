package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Client;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.Scheduling;
import com.reif.agenda_api.model.ServiceOffering;
import com.reif.agenda_api.repository.ClientRepository;
import com.reif.agenda_api.repository.ProfessionalRepository;
import com.reif.agenda_api.repository.SchedulingRepository;
import com.reif.agenda_api.repository.ServiceOfferingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SchedulingService {

    private final SchedulingRepository schedulingRepository;
    private final ClientRepository clientRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;

    public SchedulingService(SchedulingRepository schedulingRepository,
                              ClientRepository clientRepository,
                              ProfessionalRepository professionalRepository,
                              ServiceOfferingRepository serviceOfferingRepository) {
        this.schedulingRepository = schedulingRepository;
        this.clientRepository = clientRepository;
        this.professionalRepository = professionalRepository;
        this.serviceOfferingRepository = serviceOfferingRepository;
    }

    @Transactional
    public Scheduling create(Long clientId, Long professionalId, Long serviceId, LocalDateTime scheduledAt) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + clientId));

        Professional professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado: " + professionalId));

        ServiceOffering service = serviceOfferingRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado: " + serviceId));

        boolean hasConflict = !schedulingRepository
                .findByProfessionalIdAndScheduledAtBetween(
                        professionalId,
                        scheduledAt.minusMinutes(service.getDurationMinutes()),
                        scheduledAt.plusMinutes(service.getDurationMinutes()))
                .isEmpty();

        if (hasConflict) {
            throw new IllegalStateException("Profissional já possui agendamento nesse horário.");
        }

        Scheduling scheduling = new Scheduling();
        scheduling.setClient(client);
        scheduling.setProfessional(professional);
        scheduling.setService(service);
        scheduling.setScheduledAt(scheduledAt);
        scheduling.setCancellationToken(UUID.randomUUID().toString());
        scheduling.setCanceled(false);

        return schedulingRepository.save(scheduling);
    }

    public List<Scheduling> findAll() {
        return schedulingRepository.findAll();
    }

    public Scheduling findById(Long id) {
        return schedulingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado: " + id));
    }

    public List<Scheduling> findByClient(Long clientId) {
        return schedulingRepository.findByClientId(clientId);
    }

    public List<Scheduling> findByProfessional(Long professionalId) {
        return schedulingRepository.findByProfessionalId(professionalId);
    }

    public List<Scheduling> findActive() {
        return schedulingRepository.findByCanceledFalse();
    }

    @Transactional
    public void cancel(Long id) {
        Scheduling scheduling = findById(id);
        scheduling.setCanceled(true);
    }

    @Transactional
    public void cancelByToken(String token) {
        Scheduling scheduling = schedulingRepository.findByCancellationToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Token de cancelamento inválido."));
        scheduling.setCanceled(true);
    }

    @Transactional
    public void delete(Long id) {
        Scheduling scheduling = findById(id);
        schedulingRepository.delete(scheduling);
    }
}