package com.reif.agenda_api.service;

import com.reif.agenda_api.dto.ServiceOfferingRequestDTO;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.ServiceOffering;
import com.reif.agenda_api.repository.ServiceOfferingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceOfferingService {

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProfessionalService professionalService;

    public ServiceOfferingService(ServiceOfferingRepository serviceOfferingRepository,
                                   ProfessionalService professionalService) {
        this.serviceOfferingRepository = serviceOfferingRepository;
        this.professionalService = professionalService;
    }

    @Transactional
    public ServiceOffering create(Long professionalId, ServiceOfferingRequestDTO request) {
        Professional professional = professionalService.findById(professionalId);

        ServiceOffering service = new ServiceOffering();
        service.setProfessional(professional);
        applyRequest(service, request);

        return serviceOfferingRepository.save(service);
    }

    public List<ServiceOffering> findAllByProfessional(Long professionalId) {
        professionalService.findById(professionalId);
        return serviceOfferingRepository.findByProfessionalId(professionalId);
    }

    public ServiceOffering findOne(Long professionalId, Long id) {
        return findEntity(professionalId, id);
    }

    @Transactional
    public ServiceOffering update(Long professionalId, Long id, ServiceOfferingRequestDTO request) {
        ServiceOffering service = findEntity(professionalId, id);
        applyRequest(service, request);
        return serviceOfferingRepository.save(service);
    }

    @Transactional
    public void delete(Long professionalId, Long id) {
        ServiceOffering service = findEntity(professionalId, id);
        serviceOfferingRepository.delete(service);
    }

    private ServiceOffering findEntity(Long professionalId, Long id) {
        return serviceOfferingRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));
    }

    private void applyRequest(ServiceOffering service, ServiceOfferingRequestDTO request) {
        service.setName(request.name());
        service.setImageUrl(request.imageUrl());
        service.setPrice(request.price());
        service.setDescription(request.description());
        service.setDurationMinutes(request.durationMinutes());
        service.setAcceptsInspiration(request.acceptsInspiration());
    }
}
