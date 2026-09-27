package com.reif.agenda_api.dto;

import com.reif.agenda_api.model.Scheduling;

import java.time.LocalDateTime;

public class SchedulingResponseDTO {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long professionalId;
    private String professionalName;
    private Long serviceId;
    private String serviceName;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private boolean canceled;
    private String imageUrl;

    public SchedulingResponseDTO(Scheduling scheduling) {
        this.id = scheduling.getId();
        this.clientId = scheduling.getClient().getId();
        this.clientName = scheduling.getClient().getName();
        this.professionalId = scheduling.getProfessional().getId();
        this.professionalName = scheduling.getProfessional().getName();
        this.serviceId = scheduling.getService().getId();
        this.serviceName = scheduling.getService().getName();
        this.scheduledAt = scheduling.getScheduledAt();
        this.createdAt = scheduling.getCreatedAt();
        this.canceled = scheduling.isCanceled();
        this.imageUrl = scheduling.getImageUrl();
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public String getProfessionalName() {
        return professionalName;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}