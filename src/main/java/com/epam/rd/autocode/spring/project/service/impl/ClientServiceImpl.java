package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper mapper;

    @Override
    public Page<ClientDisplayDTO> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable).map(client -> mapper.map(client, ClientDisplayDTO.class));
    }

    @Override
    public ClientDisplayDTO getClientByEmail(String email) {
        return clientRepository.findByEmail(email).map(client -> mapper.map(client, ClientDisplayDTO.class))
                .orElseThrow(() -> new NotFoundException(String.format("Client with email %s not found", email)));
    }

    @Override
    public ClientDisplayDTO updateClientByEmail(String email, ClientDTO client) {
        return null;
    }

    @Override
    public void deleteClientByEmail(String email) {

    }

    @Override
    public ClientDisplayDTO addClient(ClientDTO client) {
        return null;
    }
    //TODO Place your code here
}
