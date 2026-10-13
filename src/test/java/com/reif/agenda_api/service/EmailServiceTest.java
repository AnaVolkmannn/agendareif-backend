package com.reif.agenda_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, "no-reply@reifbeautystudio.com");
    }

    @Test
    void deveMontarEEnviarEmailComOsDadosCorretos() {
        emailService.send("cliente@email.com", "Assunto de teste", "Corpo do e-mail de teste");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getFrom()).isEqualTo("no-reply@reifbeautystudio.com");
        assertThat(sent.getTo()).containsExactly("cliente@email.com");
        assertThat(sent.getSubject()).isEqualTo("Assunto de teste");
        assertThat(sent.getText()).isEqualTo("Corpo do e-mail de teste");
    }
}