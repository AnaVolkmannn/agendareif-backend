package com.reif.agenda_api.dto;

public class ProfessionalCreateResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String temporaryPassword; // exibido só nesta resposta, uma única vez

    public ProfessionalCreateResponseDTO(Long id, String name, String email, String temporaryPassword) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.temporaryPassword = temporaryPassword;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTemporaryPassword() { return temporaryPassword; }
}