package com.reif.agenda_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service")
@NoArgsConstructor
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Mantido por compatibilidade: espelha a primeira foto. Os serviços
     * cadastrados antes da lista só têm este campo preenchido.
     */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    /** Fotos do serviço, na ordem em que devem aparecer (no máximo 5). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "service_photo", joinColumns = @JoinColumn(name = "service_id"))
    @OrderColumn(name = "position")
    @Column(name = "url", columnDefinition = "TEXT")
    private List<String> photos = new ArrayList<>();

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Positive
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "accepts_inspiration", nullable = false)
    private boolean acceptsInspiration = false;

    // --- Getters e setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        // Substituir a lista confunde o Hibernate no update; mexemos na que existe.
        this.photos.clear();
        if (photos != null) {
            this.photos.addAll(photos);
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isAcceptsInspiration() {
        return acceptsInspiration;
    }

    public void setAcceptsInspiration(boolean acceptsInspiration) {
        this.acceptsInspiration = acceptsInspiration;
    }
}