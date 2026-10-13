package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Scheduling;
import com.reif.agenda_api.repository.SchedulingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingReminderSchedulerTest {

    @Mock
    private SchedulingRepository schedulingRepository;

    @Mock
    private SchedulingEmailService schedulingEmailService;

    private SchedulingReminderScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SchedulingReminderScheduler(schedulingRepository, schedulingEmailService, 3L);
    }

    @Test
    void deveEnviarLembreteEMarcarComoEnviadoParaAgendamentosPendentes() {
        Scheduling scheduling = new Scheduling();
        scheduling.setScheduledAt(LocalDateTime.now().plusHours(2));

        when(schedulingRepository.findByCanceledFalseAndReminderSentFalseAndScheduledAtBetween(any(), any()))
                .thenReturn(List.of(scheduling));

        scheduler.sendPendingReminders();

        verify(schedulingEmailService).sendReminder(scheduling);
        assertThat(scheduling.isReminderSent()).isTrue();
    }

    @Test
    void naoDeveFazerNadaQuandoNaoHaAgendamentosPendentes() {
        when(schedulingRepository.findByCanceledFalseAndReminderSentFalseAndScheduledAtBetween(any(), any()))
                .thenReturn(List.of());

        scheduler.sendPendingReminders();

        verify(schedulingEmailService, never()).sendReminder(any());
    }
}