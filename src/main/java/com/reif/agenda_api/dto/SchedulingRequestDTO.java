package com.reif.agenda_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    /** Imagem de inspiração enviada pelo cliente (RF05). Opcional. */
    private String imageUrl;

    /** Observação do cliente para o profissional. Opcional, até 300 caracteres. */
    @Size(max = 300, message = "A observação deve ter no máximo 300 caracteres")
    private String observation;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}