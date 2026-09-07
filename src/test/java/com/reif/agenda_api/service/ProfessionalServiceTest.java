package com.reif.agenda_api.service;

import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.repository.ProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfessionalService professionalService;

    private Professional professional;

    @BeforeEach
    void setUp() {
        professional = new Professional();
        professional.setId(1L);
        professional.setName("João Silva");
        professional.setEmail("joao@email.com");
        professional.setPassword("senha123");
        professional.setPhone("47999999999");
        professional.setProfilePicture("foto.png");
        professional.setDescription("Descrição do profissional");
        professional.setScheduleMode(Professional.ScheduleMode.ONLINE);
    }

    @Test
    void deveRegistrarProfissionalComSucesso() {
        when(professionalRepository.existsByEmail(professional.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);

        Professional result = professionalService.register(professional);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("joao@email.com");
        verify(passwordEncoder).encode("senha123");
        verify(professionalRepository).save(professional);
    }

    @Test
    void deveLancarExcecaoAoRegistrarComEmailJaExistente() {
        when(professionalRepository.existsByEmail(professional.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> professionalService.register(professional))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe um profissional cadastrado com esse e-mail.");

        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void deveEncontrarProfissionalPorId() {
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));

        Professional result = professionalService.findById(1L);

        assertThat(result).isEqualTo(professional);
    }

    @Test
    void deveLancarExcecaoQuandoIdNaoEncontrado() {
        when(professionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> professionalService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profissional não encontrado.");
    }

    @Test
    void deveEncontrarProfissionalPorEmail() {
        when(professionalRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(professional));

        Professional result = professionalService.findByEmail("joao@email.com");

        assertThat(result).isEqualTo(professional);
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoEncontrado() {
        when(professionalRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> professionalService.findByEmail("naoexiste@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profissional não encontrado.");
    }

    @Test
    void deveListarTodosOsProfissionais() {
        when(professionalRepository.findAll()).thenReturn(List.of(professional));

        List<Professional> result = professionalService.findAll();

        assertThat(result).hasSize(1).containsExactly(professional);
    }

    @Test
    void deveAtualizarProfissionalComSucesso() {
        Professional dadosAtualizados = new Professional();
        dadosAtualizados.setName("João Atualizado");
        dadosAtualizados.setPhone("47988888888");
        dadosAtualizados.setProfilePicture("nova-foto.png");
        dadosAtualizados.setScheduleMode(Professional.ScheduleMode.MANUAL);

        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);

        Professional result = professionalService.update(1L, dadosAtualizados);

        assertThat(result.getName()).isEqualTo("João Atualizado");
        assertThat(result.getPhone()).isEqualTo("47988888888");
        assertThat(result.getProfilePicture()).isEqualTo("nova-foto.png");
        assertThat(result.getScheduleMode()).isEqualTo(Professional.ScheduleMode.MANUAL);
        verify(professionalRepository).save(professional);
    }

    @Test
    void deveAtualizarSenhaComSucesso() {
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaSenhaCriptografada");

        professionalService.updatePassword(1L, "novaSenha");

        assertThat(professional.getPassword()).isEqualTo("novaSenhaCriptografada");
        verify(professionalRepository).save(professional);
    }

    @Test
    void deveDeletarProfissionalComSucesso() {
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        doNothing().when(professionalRepository).delete(professional);

        professionalService.delete(1L);

        verify(professionalRepository).delete(professional);
    }

    @Test
    void deveLancarExcecaoAoDeletarProfissionalInexistente() {
        when(professionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> professionalService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Profissional não encontrado.");

        verify(professionalRepository, never()).delete(any(Professional.class));
    }
}