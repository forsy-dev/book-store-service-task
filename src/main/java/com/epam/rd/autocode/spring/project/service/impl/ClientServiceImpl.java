package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.InvalidPasswordException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.ClientBlockStatus;
import com.epam.rd.autocode.spring.project.repo.ClientBlockStatusRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientBlockStatusRepository clientBlockStatusRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<ClientDisplayDTO> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable).map(this::mapToClientDisplayDTO);
    }

    @Override
    public ClientDisplayDTO getClientByEmail(String email) {
        return clientRepository.findByEmail(email).map(this::mapToClientDisplayDTO)
                .orElseThrow(() -> new NotFoundException(String.format("Client with email %s not found", email)));
    }

    @Override
    public ClientDisplayDTO updateClientByEmail(String email, ClientUpdateDTO dto) {
        log.info("Attempting to update client with email {}", email);
        Client client = clientRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(String.format("Client with email %s not found", email)));
        if (!email.equals(dto.getEmail()) && (clientRepository.existsByEmail(dto.getEmail()) ||
        employeeRepository.existsByEmail(dto.getEmail()))) {
            throw new AlreadyExistException(String.format("User with email %s already exists", dto.getEmail()));
        }
        ClientBlockStatus clientBlockStatus = clientBlockStatusRepository.findByClientEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format("Client with email %s not found", email)));
        mapper.map(dto, client);
        client = clientRepository.save(client);
        clientBlockStatus.setClientEmail(dto.getEmail());
        clientBlockStatusRepository.save(clientBlockStatus);
        log.info("Client with email {} updated successfully", email);
        return mapToClientDisplayDTO(client);
    }

    @Override
    public void deleteClientByEmail(String email) {
        log.info("Attempting to delete client with email {}", email);
        clientRepository.findByEmail(email).ifPresentOrElse(client -> {
                    ClientBlockStatus clientBlockStatus = clientBlockStatusRepository.findByClientEmail(email)
                                    .orElseThrow(() -> new NotFoundException(String.format("Client with email %s not found", email)));
                    clientRepository.delete(client);
                    clientBlockStatusRepository.delete(clientBlockStatus);
                    log.info("Client with email {} deleted successfully", email);
                },
                () -> {
                    throw new NotFoundException(String.format("Client with email %s not found", email));
                });
    }

    @Override
    public ClientDisplayDTO addClient(ClientCreateDTO dto) {
        log.info("Attempting to add client with email {}", dto.getEmail());
        if (clientRepository.existsByEmail(dto.getEmail()) || employeeRepository.existsByEmail(dto.getEmail()) ||
        clientBlockStatusRepository.existsByClientEmail(dto.getEmail())) {
            throw new AlreadyExistException(String.format("Client with email %s already exists", dto.getEmail()));
        }
        Client client = mapper.map(dto, Client.class);
        client.setPassword(passwordEncoder.encode(dto.getPassword()));
        client.setBalance(BigDecimal.ZERO);
        client = clientRepository.save(client);
        clientBlockStatusRepository.save(ClientBlockStatus.builder().clientEmail(dto.getEmail()).build());
        log.info("Client with email {} added successfully", client.getEmail());
        return mapToClientDisplayDTO(client);
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

    @Override
    public ClientDisplayDTO addBalanceToClient(String email, AddBalanceDTO dto) {
        log.info("Attempting to add balance {} to client with email {}", dto.getAmount(), email);
        Client client = clientRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(String.format("Client with email %s not found", email)));
        client.setBalance(client.getBalance().add(dto.getAmount()));
        client = clientRepository.save(client);
        log.info("Balance {} added to client with email {}", dto.getAmount(), email);
        return mapToClientDisplayDTO(client);
    }

    @Override
    public void blockClient(String email) {
        changeIsBlockStatus(email, true);
    }

    @Override
    public void unblockClient(String email) {
        changeIsBlockStatus(email, false);
    }

    private void changeIsBlockStatus(String email, boolean isBlocked) {
        ClientBlockStatus clientBlockStatus = clientBlockStatusRepository.findByClientEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format("Client with email %s not found", email)));
        clientBlockStatus.setBlocked(isBlocked);
        clientBlockStatusRepository.save(clientBlockStatus);
    }

    private ClientDisplayDTO mapToClientDisplayDTO(Client client) {
        ClientBlockStatus status = clientBlockStatusRepository.findByClientEmail(client.getEmail())
                .orElseThrow(() -> new NotFoundException(String.format("Client with email %s not found", client.getEmail())));
        ClientDisplayDTO dto = mapper.map(client, ClientDisplayDTO.class);
        dto.setIsBlocked(status.isBlocked());

        return dto;
    }
}
