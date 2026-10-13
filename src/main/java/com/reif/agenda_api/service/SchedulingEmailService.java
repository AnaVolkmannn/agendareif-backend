package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Scheduling;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class SchedulingEmailService {

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final EmailService emailService;
    private final String frontendUrl;

    public SchedulingEmailService(EmailService emailService,
                                   @Value("${app.frontend-url}") String frontendUrl) {
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    /** RF11 + RF12: confirmação para cliente e profissional, com link de cancelamento. */
    public void sendConfirmation(Scheduling scheduling) {
        String dataHora = scheduling.getScheduledAt().format(DATA_HORA);
        String cancelLink = frontendUrl + "/cancelamento/" + scheduling.getCancellationToken();

        String corpoCliente = """
                Olá, %s!

                Seu agendamento foi confirmado:

                Serviço: %s
                Profissional: %s
                Data e horário: %s

                Se precisar cancelar, acesse o link abaixo:
                %s

                Reif Beauty Studio
                """.formatted(
                scheduling.getClient().getName(),
                scheduling.getService().getName(),
                scheduling.getProfessional().getName(),
                dataHora,
                cancelLink
        );

        emailService.send(
                scheduling.getClient().getEmail(),
                "Agendamento confirmado - Reif Beauty Studio",
                corpoCliente
        );

        String corpoProfissional = """
                Olá, %s!

                Você tem um novo agendamento:

                Cliente: %s
                Telefone: %s
                Serviço: %s
                Data e horário: %s

                Reif Beauty Studio
                """.formatted(
                scheduling.getProfessional().getName(),
                scheduling.getClient().getName(),
                scheduling.getClient().getPhone(),
                scheduling.getService().getName(),
                dataHora
        );

        emailService.send(
                scheduling.getProfessional().getEmail(),
                "Novo agendamento - Reif Beauty Studio",
                corpoProfissional
        );
    }

    /** RF13: alerta de cancelamento para cliente e profissional. */
    public void sendCancellationAlert(Scheduling scheduling) {
        String dataHora = scheduling.getScheduledAt().format(DATA_HORA);

        String corpoCliente = """
                Olá, %s!

                Seu agendamento foi cancelado:

                Serviço: %s
                Profissional: %s
                Data e horário: %s

                Se quiser, acesse o site para agendar um novo horário.

                Reif Beauty Studio
                """.formatted(
                scheduling.getClient().getName(),
                scheduling.getService().getName(),
                scheduling.getProfessional().getName(),
                dataHora
        );

        emailService.send(
                scheduling.getClient().getEmail(),
                "Agendamento cancelado - Reif Beauty Studio",
                corpoCliente
        );

        String corpoProfissional = """
                Olá, %s!

                O agendamento abaixo foi cancelado:

                Cliente: %s
                Serviço: %s
                Data e horário: %s

                O horário já está liberado na sua agenda.

                Reif Beauty Studio
                """.formatted(
                scheduling.getProfessional().getName(),
                scheduling.getClient().getName(),
                scheduling.getService().getName(),
                dataHora
        );

        emailService.send(
                scheduling.getProfessional().getEmail(),
                "Agendamento cancelado - Reif Beauty Studio",
                corpoProfissional
        );

        
    }

    /** RF14: lembrete enviado ao cliente algumas horas antes do atendimento. */
    public void sendReminder(Scheduling scheduling) {
        String dataHora = scheduling.getScheduledAt().format(DATA_HORA);

        String corpo = """
                Olá, %s!

                Passando pra lembrar do seu atendimento:

                Serviço: %s
                Profissional: %s
                Data e horário: %s

                Te esperamos!

                Reif Beauty Studio
                """.formatted(
                scheduling.getClient().getName(),
                scheduling.getService().getName(),
                scheduling.getProfessional().getName(),
                dataHora
        );

        emailService.send(
                scheduling.getClient().getEmail(),
                "Lembrete de atendimento - Reif Beauty Studio",
                corpo
        );
    }
    
}