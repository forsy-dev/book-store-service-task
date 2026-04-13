package com.forsy.service.impl;

import com.forsy.dto.AddBalanceDto;
import com.forsy.dto.ChangePasswordDto;
import com.forsy.dto.ClientCreateDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.ClientUpdateDto;
import com.forsy.exception.AlreadyExistException;
import com.forsy.exception.InvalidPasswordException;
import com.forsy.exception.NotFoundException;
import com.forsy.model.Client;
import com.forsy.model.ClientBlockStatus;
import com.forsy.repo.ClientBlockStatusRepository;
import com.forsy.repo.ClientRepository;
import com.forsy.repo.EmployeeRepository;
import com.forsy.repo.OrderRepository;
import com.forsy.service.ClientService;
import java.math.BigDecimal;
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

/**
 * Concrete implementation of {@link ClientService} for managing customer data
 * and administrative status.
 *
 * <p>This service orchestrates complex workflows involving multiple
 * repositories to ensure transactional integrity during sensitive operations
 * such as account creation, deletion, and financial updates. It utilizes
 * localized message reporting and security-grade password hashing.
 *
 * @author Illia
 */
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

  /**
   * {@inheritDoc}
   *
   * <p>Optionally filters results by name or email using a case-insensitive
   * keyword search.
   */
  @Override
  public Page<ClientDisplayDto> getAllClients(Pageable pageable, String keyword) {
    Page<Client> clients;
    if (keyword != null && !keyword.trim().isEmpty()) {
      clients = clientRepository.findAllByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
          keyword, keyword, pageable);
    } else {
      clients = clientRepository.findAll(pageable);
    }
    return clients.map(this::mapToClientDisplayDto);
  }

  /**
   * {@inheritDoc}
   *
   * @throws NotFoundException if the client is not found in the primary repository
   */
  @Override
  public ClientDisplayDto getClientByEmail(String email) {
    return clientRepository.findByEmail(email).map(this::mapToClientDisplayDto).orElseThrow(() -> {
      String message = messageSource.getMessage("error.user.not.found", new Object[]{email},
                                                LocaleContextHolder.getLocale());
      return new NotFoundException(message);
    });
  }

  /**
   * {@inheritDoc}
   *
   * @throws NotFoundException if no client exists with the provided email
   */
  @Override
  public ClientDisplayDto updateClientByEmail(String email, ClientUpdateDto dto) {
    log.info("Attempting to update client with email {}", email);

    Client client = clientRepository.findByEmail(email).orElseThrow(() -> {
      String message = messageSource.getMessage("error.user.not.found", new Object[]{email},
                                                LocaleContextHolder.getLocale());
      return new NotFoundException(message);
    });
    mapper.map(dto, client);
    client = clientRepository.save(client);

    log.info("Client with email {} updated successfully", email);
    return mapper.map(client, ClientDisplayDto.class);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Executes as a single transaction to ensure orders, block records,
   * and the client identity are purged simultaneously.
   *
   * @throws NotFoundException if the status record associated with
   *                           the client is missing
   */
  @Override
  @Transactional
  public void deleteClientByEmail(String email) {
    log.info("Attempting to delete client with email {}", email);
    clientRepository.findByEmail(email).ifPresentOrElse(client -> {
      orderRepository.deleteAllByClientEmail(email);
      ClientBlockStatus clientBlockStatus =
          clientBlockStatusRepository.findByClientEmail(email).orElseThrow(() -> {
            String message = messageSource.getMessage("error.user.not.found", new Object[]{email},
                                                      LocaleContextHolder.getLocale());
            return new NotFoundException(message);
          });
      clientRepository.delete(client);
      clientBlockStatusRepository.delete(clientBlockStatus);
      log.info("Client with email {} deleted successfully", email);
    }, () -> {
      String message = messageSource.getMessage("error.user.not.found", new Object[]{email},
                                                LocaleContextHolder.getLocale());
      throw new NotFoundException(message);
    });
  }

  /**
   * {@inheritDoc}
   *
   * <p>Performs a cross-check across all identity repositories (Client,
   * Employee, and BlockStatus) to prevent email collision. Encodes the
   * password before persistence and initializes balance and block status.
   *
   * @throws AlreadyExistException if the email is registered to any user
   *                               type or block record
   */
  @Override
  @Transactional
  public ClientDisplayDto addClient(ClientCreateDto dto) {
    log.info("Attempting to add client with email {}", dto.getEmail());
    if (clientRepository.existsByEmail(dto.getEmail())
        || employeeRepository.existsByEmail(dto.getEmail())
        || clientBlockStatusRepository.existsByClientEmail(dto.getEmail())) {
      String message =
          messageSource.getMessage("error.user.already.exist",
                                   new Object[]{dto.getEmail()}, LocaleContextHolder.getLocale());
      throw new AlreadyExistException(message);
    }
    Client client = mapper.map(dto, Client.class);
    client.setPassword(passwordEncoder.encode(dto.getPassword()));
    client.setBalance(BigDecimal.ZERO);
    client = clientRepository.save(client);
    clientBlockStatusRepository.save(ClientBlockStatus.builder().clientEmail(
        dto.getEmail()).build());
    log.info("Client with email {} added successfully", client.getEmail());
    return mapToClientDisplayDto(client);
  }

  /**
   * {@inheritDoc}
   *
   * @throws InvalidPasswordException if the provided current password
   *                                  does not match the hashed record
   */
  @Override
  public void changePassword(String email, ChangePasswordDto dto) {
    log.info("Attempting to change password for client with email {}", email);
    Client client = clientRepository.findByEmail(email).orElseThrow(() -> {
      String message = messageSource.getMessage("error.user.not.found", new Object[]{email},
                                                LocaleContextHolder.getLocale());
      return new NotFoundException(message);
    });
    if (!passwordEncoder.matches(dto.getOldPassword(), client.getPassword())) {
      String message =
          messageSource.getMessage("error.user.old.password.not.match", new Object[]{email},
                                   LocaleContextHolder.getLocale());
      throw new InvalidPasswordException(message);
    }
    client.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    clientRepository.save(client);
    log.info("Password for client with email {} changed successfully", email);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ClientDisplayDto addBalanceToClient(String email, AddBalanceDto dto) {
    log.info("Attempting to add balance {} to client with email {}", dto.getAmount(), email);
    Client client = clientRepository.findByEmail(email).orElseThrow(() -> {
      String message = messageSource.getMessage("error.user.not.found", new Object[]{email},
                                                LocaleContextHolder.getLocale());
      return new NotFoundException(message);
    });
    client.setBalance(client.getBalance().add(dto.getAmount()));
    client = clientRepository.save(client);
    log.info("Balance {} added to client with email {}", dto.getAmount(), email);
    return mapToClientDisplayDto(client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void blockClient(String email) {
    changeIsBlockStatus(email, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unblockClient(String email) {
    changeIsBlockStatus(email, false);
  }

  /**
   * Internal helper to update the administrative block record.
   */
  private void changeIsBlockStatus(String email, boolean isBlocked) {
    ClientBlockStatus clientBlockStatus =
        clientBlockStatusRepository.findByClientEmail(email).orElseThrow(() -> {
          String message =
              messageSource.getMessage("error.user.not.found", new Object[]{email},
                                       LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });
    clientBlockStatus.setBlocked(isBlocked);
    clientBlockStatusRepository.save(clientBlockStatus);
  }

  /**
   * Internal helper to enrich client data with block status for
   * display purposes.
   */
  private ClientDisplayDto mapToClientDisplayDto(Client client) {
    ClientBlockStatus status =
        clientBlockStatusRepository.findByClientEmail(client.getEmail()).orElseThrow(() -> {
          String message = messageSource.getMessage("error.user.not.found", new Object[]{
              client.getEmail()}, LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });
    ClientDisplayDto dto = mapper.map(client, ClientDisplayDto.class);
    dto.setIsBlocked(status.isBlocked());

    return dto;
  }
}
