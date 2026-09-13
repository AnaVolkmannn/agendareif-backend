package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.WeeklySchedule;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyScheduleServiceTest {

    @Mock
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @Mock
    private ProfessionalService professionalService;

    @InjectMocks
    private WeeklyScheduleService weeklyScheduleService;

    private Professional professional;
    private WeeklySchedule segunda;

    @BeforeEach
    void setUp() {
        professional = new Professional();
        professional.setId(1L);
        professional.setName("João Silva");
        professional.setEmail("joao@email.com");

        segunda = new WeeklySchedule();
        segunda.setId(10L);
        segunda.setProfessional(professional);
        segunda.setDayOfWeek(1);
        segunda.setActive(true);
        segunda.setStartTime(LocalTime.of(9, 0));
        segunda.setEndTime(LocalTime.of(19, 0));
    }

    private WeeklySchedule novoDia(int diaSemana, boolean ativo, LocalTime inicio, LocalTime fim) {
        WeeklySchedule dia = new WeeklySchedule();
        dia.setDayOfWeek(diaSemana);
        dia.setActive(ativo);
        dia.setStartTime(inicio);
        dia.setEndTime(fim);
        return dia;
    }

    @Test
    void deveCriarHorarioSemanalComSucesso() {
        WeeklySchedule novo = novoDia(1, true, LocalTime.of(9, 0), LocalTime.of(19, 0));

        when(professionalService.findById(1L)).thenReturn(professional);
        when(weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(1L, 1)).thenReturn(false);
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(segunda);

        WeeklySchedule result = weeklyScheduleService.create(1L, novo);

        assertThat(result).isNotNull();
        assertThat(novo.getProfessional()).isEqualTo(professional);
        verify(weeklyScheduleRepository).save(novo);
    }

    @Test
    void deveCriarDiaFechadoSemInformarHorario() {
        WeeklySchedule fechado = novoDia(0, false, null, null);

        when(professionalService.findById(1L)).thenReturn(professional);
        when(weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(1L, 0)).thenReturn(false);
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(fechado);

        WeeklySchedule result = weeklyScheduleService.create(1L, fechado);

        assertThat(result.isActive()).isFalse();
        verify(weeklyScheduleRepository).save(fechado);
    }

    @Test
    void deveLancarExcecaoAoCriarDiaDaSemanaDuplicado() {
        WeeklySchedule novo = novoDia(1, true, LocalTime.of(9, 0), LocalTime.of(19, 0));

        when(professionalService.findById(1L)).thenReturn(professional);
        when(weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(1L, 1)).thenReturn(true);

        assertThatThrownBy(() -> weeklyScheduleService.create(1L, novo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esse dia da semana já está cadastrado para o profissional.");

        verify(weeklyScheduleRepository, never()).save(any(WeeklySchedule.class));
    }

    @Test
    void deveLancarExcecaoQuandoDiaAbertoNaoTemHorario() {
        WeeklySchedule semHorario = novoDia(2, true, null, null);

        when(professionalService.findById(1L)).thenReturn(professional);
        when(weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(1L, 2)).thenReturn(false);

        assertThatThrownBy(() -> weeklyScheduleService.create(1L, semHorario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Informe o horário de início e fim para um dia aberto.");

        verify(weeklyScheduleRepository, never()).save(any(WeeklySchedule.class));
    }

    @Test
    void deveLancarExcecaoQuandoInicioNaoEAnteriorAoFim() {
        WeeklySchedule invertido = novoDia(3, true, LocalTime.of(19, 0), LocalTime.of(9, 0));

        when(professionalService.findById(1L)).thenReturn(professional);
        when(weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(1L, 3)).thenReturn(false);

        assertThatThrownBy(() -> weeklyScheduleService.create(1L, invertido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O horário de início deve ser anterior ao horário de fim.");

        verify(weeklyScheduleRepository, never()).save(any(WeeklySchedule.class));
    }

    @Test
    void deveListarHorariosDoProfissional() {
        when(professionalService.findById(1L)).thenReturn(professional);
        when(weeklyScheduleRepository.findByProfessionalIdOrderByDayOfWeekAsc(1L)).thenReturn(List.of(segunda));

        List<WeeklySchedule> result = weeklyScheduleService.findAllByProfessional(1L);

        assertThat(result).hasSize(1).containsExactly(segunda);
    }

    @Test
    void deveEncontrarHorarioPorId() {
        when(weeklyScheduleRepository.findByIdAndProfessionalId(10L, 1L)).thenReturn(Optional.of(segunda));

        WeeklySchedule result = weeklyScheduleService.findById(1L, 10L);

        assertThat(result).isEqualTo(segunda);
    }

    @Test
    void deveLancarExcecaoQuandoHorarioNaoEncontrado() {
        when(weeklyScheduleRepository.findByIdAndProfessionalId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> weeklyScheduleService.findById(1L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Horário semanal não encontrado.");
    }

    @Test
    void deveEncontrarHorarioPorDiaDaSemana() {
        when(weeklyScheduleRepository.findByProfessionalIdAndDayOfWeek(1L, 1)).thenReturn(Optional.of(segunda));

        WeeklySchedule result = weeklyScheduleService.findByDayOfWeek(1L, 1);

        assertThat(result).isEqualTo(segunda);
    }

    @Test
    void deveAtualizarHorarioComSucesso() {
        WeeklySchedule dados = novoDia(1, true, LocalTime.of(13, 0), LocalTime.of(17, 0));

        when(weeklyScheduleRepository.findByIdAndProfessionalId(10L, 1L)).thenReturn(Optional.of(segunda));
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(segunda);

        WeeklySchedule result = weeklyScheduleService.update(1L, 10L, dados);

        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(13, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(17, 0));
        verify(weeklyScheduleRepository).save(segunda);
    }

    @Test
    void deveLancarExcecaoAoAtualizarParaDiaJaCadastrado() {
        WeeklySchedule dados = novoDia(2, true, LocalTime.of(9, 0), LocalTime.of(19, 0));

        when(weeklyScheduleRepository.findByIdAndProfessionalId(10L, 1L)).thenReturn(Optional.of(segunda));
        when(weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(1L, 2)).thenReturn(true);

        assertThatThrownBy(() -> weeklyScheduleService.update(1L, 10L, dados))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Esse dia da semana já está cadastrado para o profissional.");

        verify(weeklyScheduleRepository, never()).save(any(WeeklySchedule.class));
    }

    @Test
    void deveSubstituirSemanaInteiraEmLote() {
        List<WeeklySchedule> novaSemana = List.of(
                novoDia(1, true, LocalTime.of(13, 0), LocalTime.of(17, 0)),
                novoDia(2, true, LocalTime.of(13, 0), LocalTime.of(17, 0)),
                novoDia(3, false, null, null)
        );

        when(professionalService.findById(1L)).thenReturn(professional);
        when(weeklyScheduleRepository.findByProfessionalIdOrderByDayOfWeekAsc(1L)).thenReturn(List.of(segunda));
        when(weeklyScheduleRepository.saveAll(anyList())).thenReturn(novaSemana);

        List<WeeklySchedule> result = weeklyScheduleService.replaceWeek(1L, novaSemana);

        assertThat(result).hasSize(3);
        assertThat(novaSemana).allMatch(dia -> dia.getProfessional().equals(professional));
        verify(weeklyScheduleRepository).deleteAll(List.of(segunda));
        verify(weeklyScheduleRepository).flush();
        verify(weeklyScheduleRepository).saveAll(novaSemana);
    }

    @Test
    void deveLancarExcecaoQuandoLoteTemDiaRepetido() {
        List<WeeklySchedule> comRepetido = List.of(
                novoDia(1, true, LocalTime.of(13, 0), LocalTime.of(17, 0)),
                novoDia(1, true, LocalTime.of(18, 0), LocalTime.of(20, 0))
        );

        when(professionalService.findById(1L)).thenReturn(professional);

        assertThatThrownBy(() -> weeklyScheduleService.replaceWeek(1L, comRepetido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O dia da semana 1 veio repetido.");

        verify(weeklyScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    void deveDeletarHorarioComSucesso() {
        when(weeklyScheduleRepository.findByIdAndProfessionalId(10L, 1L)).thenReturn(Optional.of(segunda));
        doNothing().when(weeklyScheduleRepository).delete(segunda);

        weeklyScheduleService.delete(1L, 10L);

        verify(weeklyScheduleRepository).delete(segunda);
    }

    @Test
    void deveLancarExcecaoAoDeletarHorarioInexistente() {
        when(weeklyScheduleRepository.findByIdAndProfessionalId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> weeklyScheduleService.delete(1L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Horário semanal não encontrado.");

        verify(weeklyScheduleRepository, never()).delete(any(WeeklySchedule.class));
    }
}
