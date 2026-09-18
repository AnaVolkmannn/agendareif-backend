package com.reif.agenda_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Pattern(regexp = "^\\S+(\\s+\\S+)+$", message = "Informe o nome completo (nome e sobrenome)")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$", message = "Telefone inválido. Use o formato (DDD) 90000-0000")
        String phone,

        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
        String observation

) {
}