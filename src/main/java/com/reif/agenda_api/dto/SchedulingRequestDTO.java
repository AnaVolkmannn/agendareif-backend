package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class SchedulingRequestDTO {

    @NotNull
    private Long clientId;

    @NotNull
    private Long professionalId;

    @NotNull
    private Long serviceId;

    @NotNull
    private LocalDateTime scheduledAt;

    public SchedulingRequestDTO() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(Long professionalId) {
        this.professionalId = professionalId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
}