package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Scheduling;
import com.reif.agenda_api.repository.SchedulingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RF14 - Lembrete de Atendimento.
 * Roda periodicamente procurando agendamentos ativos que começam dentro
 * da janela configurada e ainda não tiveram lembrete enviado.
 */
@Component
public class SchedulingReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(SchedulingReminderScheduler.class);

    private final SchedulingRepository schedulingRepository;
    private final SchedulingEmailService schedulingEmailService;
    private final long reminderHoursBefore;

    public SchedulingReminderScheduler(SchedulingRepository schedulingRepository,
                                        SchedulingEmailService schedulingEmailService,
                                        @Value("${app.reminder-hours-before}") long reminderHoursBefore) {
        this.schedulingRepository = schedulingRepository;
        this.schedulingEmailService = schedulingEmailService;
        this.reminderHoursBefore = reminderHoursBefore;
    }

    /**
     * Roda a cada 15 minutos (configurável via app.reminder-check-interval-ms).
     * Busca agendamentos ativos, sem lembrete enviado, cujo horário cai dentro
     * da janela [agora, agora + reminderHoursBefore]. O flag reminderSent evita
     * reenvio em execuções seguintes do job.
     */
    @Scheduled(fixedRateString = "${app.reminder-check-interval-ms}")
    @Transactional
    public void sendPendingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(reminderHoursBefore);

        List<Scheduling> pending = schedulingRepository
                .findByCanceledFalseAndReminderSentFalseAndScheduledAtBetween(now, limit);

        for (Scheduling scheduling : pending) {
            schedulingEmailService.sendReminder(scheduling);
            scheduling.setReminderSent(true);
        }

        if (!pending.isEmpty()) {
            log.info("Lembretes enviados para {} agendamento(s).", pending.size());
        }
    }
}