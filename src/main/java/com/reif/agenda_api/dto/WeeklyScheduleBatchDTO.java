package com.reif.agenda_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Cadastro em lote do horário da semana (RF24): o profissional envia todos
 * os dias de uma vez e o que estava salvo antes é substituído.
 */
public record WeeklyScheduleBatchDTO(

        @NotEmpty(message = "Informe ao menos um dia da semana")
        @Valid
        List<WeeklyScheduleRequestDTO> days
) {
}
