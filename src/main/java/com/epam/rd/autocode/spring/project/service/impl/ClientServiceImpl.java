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
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OrderRepository orderRepository;
    private final MessageSource messageSource;

    @Override
    public Page<ClientDisplayDTO> getAllClients(Pageable pageable, String keyword) {
        Page<Client> clients;
        if (keyword != null && !keyword.trim().isEmpty()) {
            clients = clientRepository.findAllByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            clients = clientRepository.findAll(pageable);
        }
        return clients.map(this::mapToClientDisplayDTO);
    }

    @Override
    public ClientDisplayDTO getClientByEmail(String email) {
        return clientRepository.findByEmail(email).map(this::mapToClientDisplayDTO)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.user.not.found",
                        new Object[]{email}, LocaleContextHolder.getLocale());
                    return new NotFoundException(message);
                });
    }

    @Override
    public ClientDisplayDTO updateClientByEmail(String email, ClientUpdateDTO dto) {
        log.info("Attempting to update client with email {}", email);

        Client client = clientRepository.findByEmail(email).orElseThrow(() -> {
            String message = messageSource.getMessage("error.user.not.found",
                new Object[]{email}, LocaleContextHolder.getLocale());
            return new NotFoundException(message);
        });
        mapper.map(dto, client);
        client = clientRepository.save(client);

        log.info("Client with email {} updated successfully", email);
        return mapper.map(client, ClientDisplayDTO.class);
    }

    @Override
    @Transactional
    public void deleteClientByEmail(String email) {
        log.info("Attempting to delete client with email {}", email);
        clientRepository.findByEmail(email).ifPresentOrElse(client -> {
                    orderRepository.deleteAllByClientEmail(email);
                    ClientBlockStatus clientBlockStatus = clientBlockStatusRepository.findByClientEmail(email)
                        .orElseThrow(() -> {
                            String message = messageSource.getMessage("error.user.not.found",
                                new Object[]{email}, LocaleContextHolder.getLocale());
                            return new NotFoundException(message);
                        });
                    clientRepository.delete(client);
                    clientBlockStatusRepository.delete(clientBlockStatus);
                    log.info("Client with email {} deleted successfully", email);
                },
                () -> {
                    String message = messageSource.getMessage("error.user.not.found",
                        new Object[]{email}, LocaleContextHolder.getLocale());
                    throw new NotFoundException(message);
                });
    }

    @Override
    public ClientDisplayDTO addClient(ClientCreateDTO dto) {
        log.info("Attempting to add client with email {}", dto.getEmail());
        if (clientRepository.existsByEmail(dto.getEmail()) || employeeRepository.existsByEmail(dto.getEmail()) ||
        clientBlockStatusRepository.existsByClientEmail(dto.getEmail())) {
            String message = messageSource.getMessage("error.user.already.exist",
                new Object[]{dto.getEmail()}, LocaleContextHolder.getLocale());
            throw new AlreadyExistException(message);
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
                () ->  {
                    String message = messageSource.getMessage("error.user.not.found",
                        new Object[]{email}, LocaleContextHolder.getLocale());
                    return new NotFoundException(message);
                });
        if (!passwordEncoder.matches(dto.getOldPassword(), client.getPassword())) {
            String message = messageSource.getMessage("error.user.old.password.not.match",
                new Object[]{email}, LocaleContextHolder.getLocale());
            throw new InvalidPasswordException(message);
        }
        client.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        clientRepository.save(client);
        log.info("Password for client with email {} changed successfully", email);
    }

    @Override
    public ClientDisplayDTO addBalanceToClient(String email, AddBalanceDTO dto) {
        log.info("Attempting to add balance {} to client with email {}", dto.getAmount(), email);
        Client client = clientRepository.findByEmail(email).orElseThrow(
                () -> {
                    String message = messageSource.getMessage("error.user.not.found",
                        new Object[]{email}, LocaleContextHolder.getLocale());
                    return new NotFoundException(message);
                });
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
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.user.not.found",
                        new Object[]{email}, LocaleContextHolder.getLocale());
                    return new NotFoundException(message);
                });
        clientBlockStatus.setBlocked(isBlocked);
        clientBlockStatusRepository.save(clientBlockStatus);
    }

    private ClientDisplayDTO mapToClientDisplayDTO(Client client) {
        ClientBlockStatus status = clientBlockStatusRepository.findByClientEmail(client.getEmail())
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.user.not.found",
                        new Object[]{client.getEmail()}, LocaleContextHolder.getLocale());
                    return new NotFoundException(message);
                });
        ClientDisplayDTO dto = mapper.map(client, ClientDisplayDTO.class);
        dto.setIsBlocked(status.isBlocked());

        return dto;
    }
}
