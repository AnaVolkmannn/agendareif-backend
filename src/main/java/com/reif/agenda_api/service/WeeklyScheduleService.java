package com.reif.agenda_api.service;

import com.reif.agenda_api.model.PauseWeeklySchedule;
import com.reif.agenda_api.model.Professional;
import com.reif.agenda_api.model.WeeklySchedule;
import com.reif.agenda_api.repository.PauseWeeklyScheduleRepository;
import com.reif.agenda_api.repository.WeeklyScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WeeklyScheduleService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] NOMES_DIAS = {
            "domingo", "segunda-feira", "terça-feira", "quarta-feira",
            "quinta-feira", "sexta-feira", "sábado"
    };

    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final PauseWeeklyScheduleRepository pauseWeeklyScheduleRepository;
    private final ProfessionalService professionalService;

    public WeeklyScheduleService(WeeklyScheduleRepository weeklyScheduleRepository,
                                  PauseWeeklyScheduleRepository pauseWeeklyScheduleRepository,
                                  ProfessionalService professionalService) {
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.pauseWeeklyScheduleRepository = pauseWeeklyScheduleRepository;
        this.professionalService = professionalService;
    }

    @Transactional
    public WeeklySchedule create(Long professionalId, WeeklySchedule data) {
        Professional professional = professionalService.findById(professionalId);

        if (weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(professionalId, data.getDayOfWeek())) {
            throw new IllegalArgumentException("Esse dia da semana já está cadastrado para o profissional.");
        }

        data.setProfessional(professional);
        validate(data);
        return weeklyScheduleRepository.save(data);
    }

    public List<WeeklySchedule> findAllByProfessional(Long professionalId) {
        professionalService.findById(professionalId);
        return weeklyScheduleRepository.findByProfessionalIdOrderByDayOfWeekAsc(professionalId);
    }

    public WeeklySchedule findById(Long professionalId, Long id) {
        return weeklyScheduleRepository.findByIdAndProfessionalId(id, professionalId)
                .orElseThrow(() -> new IllegalArgumentException("Horário semanal não encontrado."));
    }

    public WeeklySchedule findByDayOfWeek(Long professionalId, Integer dayOfWeek) {
        return weeklyScheduleRepository.findByProfessionalIdAndDayOfWeek(professionalId, dayOfWeek)
                .orElseThrow(() -> new IllegalArgumentException("Horário semanal não encontrado."));
    }

    @Transactional
    public WeeklySchedule update(Long professionalId, Long id, WeeklySchedule data) {
        WeeklySchedule weeklySchedule = findById(professionalId, id);

        // Trocar o dia da semana não pode colidir com outro dia já cadastrado.
        if (!weeklySchedule.getDayOfWeek().equals(data.getDayOfWeek())
                && weeklyScheduleRepository.existsByProfessionalIdAndDayOfWeek(professionalId, data.getDayOfWeek())) {
            throw new IllegalArgumentException("Esse dia da semana já está cadastrado para o profissional.");
        }

        weeklySchedule.setDayOfWeek(data.getDayOfWeek());
        weeklySchedule.setActive(data.isActive());
        weeklySchedule.setStartTime(data.getStartTime());
        weeklySchedule.setEndTime(data.getEndTime());

        validate(weeklySchedule);
        return weeklyScheduleRepository.save(weeklySchedule);
    }

    /**
     * Cadastro em lote (RF24): substitui o horário da semana inteiro pelo
     * que veio na requisição.
     *
     * Os dias que já existem são editados no lugar, em vez de apagados e
     * recriados. É o que preserva os intervalos: eles ficam pendurados no
     * dia, então recriar o dia levaria o almoço junto — mesmo quando o
     * almoço continua cabendo no horário novo.
     */
    @Transactional
    public List<WeeklySchedule> replaceWeek(Long professionalId, List<WeeklySchedule> days) {
        Professional professional = professionalService.findById(professionalId);

        Set<Integer> diasRecebidos = new HashSet<>();
        for (WeeklySchedule day : days) {
            if (!diasRecebidos.add(day.getDayOfWeek())) {
                throw new IllegalArgumentException("O dia da semana " + day.getDayOfWeek() + " veio repetido.");
            }
            validate(day);
        }

        Map<Integer, WeeklySchedule> naoEnviados = new LinkedHashMap<>();
        for (WeeklySchedule existente : weeklyScheduleRepository.findByProfessionalIdOrderByDayOfWeekAsc(professionalId)) {
            naoEnviados.put(existente.getDayOfWeek(), existente);
        }

        List<WeeklySchedule> semana = new ArrayList<>();
        for (WeeklySchedule day : days) {
            WeeklySchedule existente = naoEnviados.remove(day.getDayOfWeek());

            if (existente == null) {
                day.setProfessional(professional);
                semana.add(day);
                continue;
            }

            checkPausesStillFit(existente, day);
            existente.setActive(day.isActive());
            existente.setStartTime(day.getStartTime());
            existente.setEndTime(day.getEndTime());
            semana.add(existente);
        }

        // Dia que não veio na requisição deixa de existir, e o intervalo dele
        // vai junto — sem o dia não há expediente onde encaixar o almoço.
        List<WeeklySchedule> removidos = new ArrayList<>(naoEnviados.values());
        for (WeeklySchedule removido : removidos) {
            deletePauses(removido.getId());
        }
        weeklyScheduleRepository.deleteAll(removidos);
        weeklyScheduleRepository.flush();

        return weeklyScheduleRepository.saveAll(semana);
    }

    @Transactional
    public void delete(Long professionalId, Long id) {
        WeeklySchedule weeklySchedule = findById(professionalId, id);
        deletePauses(weeklySchedule.getId());
        weeklyScheduleRepository.delete(weeklySchedule);
    }

    private void deletePauses(Long weeklyScheduleId) {
        pauseWeeklyScheduleRepository.deleteAll(
                pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(weeklyScheduleId)
        );
        pauseWeeklyScheduleRepository.flush();
    }

    /**
     * Mudar o horário de um dia não pode apagar em silêncio um intervalo que
     * a profissional cadastrou. Se o intervalo não couber mais no horário
     * novo, o save é recusado com a mensagem dizendo qual intervalo remover.
     */
    private void checkPausesStillFit(WeeklySchedule existente, WeeklySchedule novo) {
        List<PauseWeeklySchedule> pauses =
                pauseWeeklyScheduleRepository.findByWeeklyScheduleIdOrderByStartTimeAsc(existente.getId());

        if (pauses.isEmpty()) {
            return;
        }

        String nomeDia = NOMES_DIAS[novo.getDayOfWeek()];

        if (!novo.isActive()) {
            throw new IllegalArgumentException(
                    "A " + nomeDia + " tem intervalo cadastrado. Remova o intervalo antes de fechar o dia."
            );
        }

        for (PauseWeeklySchedule pause : pauses) {
            if (pause.getStartTime().isBefore(novo.getStartTime())
                    || pause.getEndTime().isAfter(novo.getEndTime())) {
                throw new IllegalArgumentException(
                        "O intervalo das " + pause.getStartTime().format(HORA)
                                + " às " + pause.getEndTime().format(HORA)
                                + " não cabe no novo horário de " + nomeDia
                                + ". Remova o intervalo antes de salvar."
                );
            }
        }
    }

    /**
     * Um dia fechado não precisa de horário. Já um dia aberto precisa de
     * início e fim coerentes, senão a agenda gera horários inválidos.
     */
    private void validate(WeeklySchedule weeklySchedule) {
        if (!weeklySchedule.isActive()) {
            return;
        }

        if (weeklySchedule.getStartTime() == null || weeklySchedule.getEndTime() == null) {
            throw new IllegalArgumentException("Informe o horário de início e fim para um dia aberto.");
        }

        if (!weeklySchedule.getStartTime().isBefore(weeklySchedule.getEndTime())) {
            throw new IllegalArgumentException("O horário de início deve ser anterior ao horário de fim.");
        }
    }
}