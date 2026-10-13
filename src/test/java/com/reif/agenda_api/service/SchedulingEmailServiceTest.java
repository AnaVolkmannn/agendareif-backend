package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Client;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.Scheduling;
import com.reif.agenda_api.model.ServiceOffering;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchedulingEmailServiceTest {

    @Mock
    private EmailService emailService;

    private SchedulingEmailService schedulingEmailService;
    private Scheduling scheduling;

    @BeforeEach
    void setUp() {
        schedulingEmailService = new SchedulingEmailService(emailService, "http://localhost:3000");

        Client client = new Client();
        client.setName("Maria Souza");
        client.setEmail("maria@email.com");
        client.setPhone("47999998888");

        Professional professional = new Professional();
        professional.setName("João Silva");
        professional.setEmail("joao@email.com");

        ServiceOffering service = new ServiceOffering();
        service.setName("Manicure");

        scheduling = new Scheduling();
        scheduling.setClient(client);
        scheduling.setProfessional(professional);
        scheduling.setService(service);
        scheduling.setScheduledAt(LocalDateTime.of(2026, 10, 5, 14, 30));
        scheduling.setCancellationToken("abc-123");
    }

    @Test
    void deveEnviarConfirmacaoParaClienteEProfissionalComLinkDeCancelamento() {
        schedulingEmailService.sendConfirmation(scheduling);

        ArgumentCaptor<String> destinatarios = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpos = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(2)).send(destinatarios.capture(), org.mockito.ArgumentMatchers.anyString(), corpos.capture());

        assertThat(destinatarios.getAllValues()).containsExactly("maria@email.com", "joao@email.com");
        assertThat(corpos.getAllValues().get(0)).contains("http://localhost:3000/cancelamento/abc-123");
    }

    @Test
    void deveEnviarAlertaDeCancelamentoParaClienteEProfissional() {
        schedulingEmailService.sendCancellationAlert(scheduling);

        verify(emailService, times(2)).send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}