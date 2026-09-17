package com.reif.agenda_api.service;

import com.reif.agenda_api.model.PauseWeeklySchedule;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.WeeklySchedule;
import com.reif.agenda_api.repository.PauseWeeklyScheduleRepository;
import com.reif.agenda_api.repository.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PauseWeeklyScheduleServiceTest {

    @Mock
    private PauseWeeklyScheduleRepository pauseWeeklyScheduleRepository;

    @Mock
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @InjectMocks
    private PauseWeeklyScheduleService pauseWeeklyScheduleService;

    private WeeklySchedule segunda;

    @BeforeEach
    void setUp() {
        Professional professional = new Professional();
        professional.setId(1L);

        segunda = new WeeklySchedule();
        segunda.setId(10L);
        segunda.setProfessional(professional);
        segunda.setDayOfWeek(1);
        segunda.setActive(true);
        segunda.setStartTime(LocalTime.of(9, 0));
        segunda.setEndTime(LocalTime.of(19, 0));
    }

    private PauseWeeklySchedule novoIntervalo(LocalTime inicio, LocalTime fim) {
        PauseWeeklySchedule pause = new PauseWeeklySchedule();
        pause.setStartTime(inicio);
        pause.setEndTime(fim);
        return pause;
    }

    @Test
    void deveCriarIntervaloComSucesso() {
        PauseWeeklySchedule almoco = novoIntervalo(LocalTime.of(12, 0), LocalTime.of(13, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));
        when(pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(10L)).thenReturn(List.of());
        when(pauseWeeklyScheduleRepository.save(any(PauseWeeklySchedule.class))).thenReturn(almoco);

        PauseWeeklySchedule result = pauseWeeklyScheduleService.create(10L, almoco);

        assertThat(result).isNotNull();
        assertThat(almoco.getWeeklySchedule()).isEqualTo(segunda);
        verify(pauseWeeklyScheduleRepository).save(almoco);
    }

    @Test
    void devePermitirIntervalosEncostadosSemSobrepor() {
        PauseWeeklySchedule existente = novoIntervalo(LocalTime.of(12, 0), LocalTime.of(13, 0));
        PauseWeeklySchedule novo = novoIntervalo(LocalTime.of(13, 0), LocalTime.of(14, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));
        when(pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(10L))
                .thenReturn(List.of(existente));
        when(pauseWeeklyScheduleRepository.save(any(PauseWeeklySchedule.class))).thenReturn(novo);

        PauseWeeklySchedule result = pauseWeeklyScheduleService.create(10L, novo);

        assertThat(result).isNotNull();
        verify(pauseWeeklyScheduleRepository).save(novo);
    }

    @Test
    void deveLancarExcecaoQuandoHorarioSemanalNaoEncontrado() {
        PauseWeeklySchedule almoco = novoIntervalo(LocalTime.of(12, 0), LocalTime.of(13, 0));

        when(weeklyScheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pauseWeeklyScheduleService.create(99L, almoco))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Horário semanal não encontrado.");

        verify(pauseWeeklyScheduleRepository, never()).save(any(PauseWeeklySchedule.class));
    }

    @Test
    void deveLancarExcecaoQuandoDiaEstaFechado() {
        segunda.setActive(false);
        PauseWeeklySchedule almoco = novoIntervalo(LocalTime.of(12, 0), LocalTime.of(13, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));

        assertThatThrownBy(() -> pauseWeeklyScheduleService.create(10L, almoco))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Intervalos só podem ser cadastrados em dias abertos.");

        verify(pauseWeeklyScheduleRepository, never()).save(any(PauseWeeklySchedule.class));
    }

    @Test
    void deveLancarExcecaoQuandoDiaNaoTemHorarioDefinido() {
        segunda.setStartTime(null);
        segunda.setEndTime(null);
        PauseWeeklySchedule almoco = novoIntervalo(LocalTime.of(12, 0), LocalTime.of(13, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));

        assertThatThrownBy(() -> pauseWeeklyScheduleService.create(10L, almoco))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O dia precisa ter início e fim definidos antes de cadastrar um intervalo.");

        verify(pauseWeeklyScheduleRepository, never()).save(any(PauseWeeklySchedule.class));
    }

    @Test
    void deveLancarExcecaoQuandoFimNaoEPosteriorAoInicio() {
        PauseWeeklySchedule invertido = novoIntervalo(LocalTime.of(13, 0), LocalTime.of(12, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));

        assertThatThrownBy(() -> pauseWeeklyScheduleService.create(10L, invertido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O fim do intervalo deve ser posterior ao início.");

        verify(pauseWeeklyScheduleRepository, never()).save(any(PauseWeeklySchedule.class));
    }

    @Test
    void deveLancarExcecaoQuandoIntervaloComecaAntesDoExpediente() {
        PauseWeeklySchedule cedoDemais = novoIntervalo(LocalTime.of(8, 0), LocalTime.of(10, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));

        assertThatThrownBy(() -> pauseWeeklyScheduleService.create(10L, cedoDemais))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O intervalo deve estar dentro do horário de trabalho do dia.");

        verify(pauseWeeklyScheduleRepository, never()).save(any(PauseWeeklySchedule.class));
    }

    @Test
    void deveLancarExcecaoQuandoIntervaloTerminaDepoisDoExpediente() {
        PauseWeeklySchedule tardeDemais = novoIntervalo(LocalTime.of(18, 0), LocalTime.of(20, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));

        assertThatThrownBy(() -> pauseWeeklyScheduleService.create(10L, tardeDemais))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O intervalo deve estar dentro do horário de trabalho do dia.");

        verify(pauseWeeklyScheduleRepository, never()).save(any(PauseWeeklySchedule.class));
    }

    @Test
    void deveLancarExcecaoQuandoIntervaloSobrepoeOutro() {
        PauseWeeklySchedule existente = novoIntervalo(LocalTime.of(12, 0), LocalTime.of(13, 0));
        PauseWeeklySchedule sobreposto = novoIntervalo(LocalTime.of(12, 30), LocalTime.of(14, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));
        when(pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(10L))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> pauseWeeklyScheduleService.create(10L, sobreposto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe um intervalo cadastrado nesse horário.");

        verify(pauseWeeklyScheduleRepository, never()).save(any(PauseWeeklySchedule.class));
    }

    @Test
    void deveListarIntervalosDoDia() {
        PauseWeeklySchedule almoco = novoIntervalo(LocalTime.of(12, 0), LocalTime.of(13, 0));

        when(weeklyScheduleRepository.findById(10L)).thenReturn(Optional.of(segunda));
        when(pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(10L))
                .thenReturn(List.of(almoco));

        List<PauseWeeklySchedule> result = pauseWeeklyScheduleService.findByWeeklySchedule(10L);

        assertThat(result).hasSize(1).containsExactly(almoco);
    }

    @Test
    void deveDeletarIntervaloComSucesso() {
        PauseWeeklySchedule almoco = novoIntervalo(LocalTime.of(12, 0), LocalTime.of(13, 0));
        almoco.setId(5L);

        when(pauseWeeklyScheduleRepository.findById(5L)).thenReturn(Optional.of(almoco));
        doNothing().when(pauseWeeklyScheduleRepository).delete(almoco);

        pauseWeeklyScheduleService.delete(5L);

        verify(pauseWeeklyScheduleRepository).delete(almoco);
    }

    @Test
    void deveLancarExcecaoAoDeletarIntervaloInexistente() {
        when(pauseWeeklyScheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pauseWeeklyScheduleService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Intervalo não encontrado.");

        verify(pauseWeeklyScheduleRepository, never()).delete(any(PauseWeeklySchedule.class));
    }
}
