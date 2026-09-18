package com.reif.agenda_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reif.agenda_api.dto.ClientRequestDTO;
import com.reif.agenda_api.model.Client;
import com.reif.agenda_api.repository.ClientRepository;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Client create(ClientRequestDTO request) {
        Client client = new Client();
        applyRequest(client, request);
        return clientRepository.save(client);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findOne(Long id) {
        return findEntity(id);
    }

    @Transactional
    public Client update(Long id, ClientRequestDTO request) {
        Client client = findEntity(id);
        applyRequest(client, request);
        return clientRepository.save(client);
    }

    @Transactional
    public void delete(Long id) {
        Client client = findEntity(id);
        clientRepository.delete(client);
    }

    private Client findEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
    }

    private void applyRequest(Client client, ClientRequestDTO request) {
        client.setName(request.name());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setObservation(request.observation());
    }
}