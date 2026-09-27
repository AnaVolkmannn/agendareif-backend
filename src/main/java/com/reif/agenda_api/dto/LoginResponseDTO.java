package com.reif.agenda_api.dto;

public class LoginResponseDTO {

    private String token;
    private String role;
    private Long id;
    private String email;
    private boolean mustChangePassword;

    public LoginResponseDTO(String token, String role, Long id, String email, boolean mustChangePassword) {
        this.token = token;
        this.role = role;
        this.id = id;
        this.email = email;
        this.mustChangePassword = mustChangePassword;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}