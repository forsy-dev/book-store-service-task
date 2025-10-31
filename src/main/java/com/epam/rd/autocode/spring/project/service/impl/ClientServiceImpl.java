package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ChangePasswordDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.InvalidPasswordException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;

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
    public ClientDisplayDTO updateClientByEmail(String email, ClientUpdateDTO dto) {
        log.info("Attempting to update client with email {}", email);
        Client client = clientRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(String.format("Client with email %s not found", email)));
        if (!email.equals(dto.getEmail()) && clientRepository.existsByEmail(dto.getEmail())) {
            throw new AlreadyExistException(String.format("Client with email %s already exists", dto.getEmail()));
        }
        mapper.map(dto, client);
        client = clientRepository.save(client);
        log.info("Client with email {} updated successfully", email);
        return mapper.map(client, ClientDisplayDTO.class);
    }

    @Override
    public void deleteClientByEmail(String email) {
        log.info("Attempting to delete client with email {}", email);
        clientRepository.findByEmail(email).ifPresentOrElse(client -> {
                    clientRepository.delete(client);
                    log.info("Client with email {} deleted successfully", email);
                },
                () -> {
                    throw new NotFoundException(String.format("Client with email %s not found", email));
                });
    }

    @Override
    public ClientDisplayDTO addClient(ClientDTO client) {
        return null;
    }

    @Override
    public void changePassword(String email, ChangePasswordDTO dto) {
        log.info("Attempting to change password for client with email {}", email);
        Client client = clientRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(String.format("Client with email %s not found", email)));
        if (!passwordEncoder.matches(dto.getOldPassword(), client.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }
        client.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        clientRepository.save(client);
        log.info("Password for client with email {} changed successfully", email);
    }
}
