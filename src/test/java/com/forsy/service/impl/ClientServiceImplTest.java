package com.forsy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.forsy.util.MessageKeys;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

  @InjectMocks
  private ClientServiceImpl clientService;

  @Mock
  private ClientRepository clientRepository;

  @Mock
  private EmployeeRepository employeeRepository;

  @Mock
  private ClientBlockStatusRepository clientBlockStatusRepository;

  @Mock
  private ModelMapper mapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private MessageSource messageSource;

  @Test
  void testGetAllClientsShouldReturnPagedClients() {
    Client client = Client.builder().build();
    ClientDisplayDto expectedDto = new ClientDisplayDto();
    Pageable pageable = PageRequest.of(0, 10);
    Page<Client> clientPage = new PageImpl<>(Collections.singletonList(client), pageable, 1);
    ClientBlockStatus clientBlockStatus = ClientBlockStatus.builder().isBlocked(false).build();

    when(clientRepository.findAll(pageable)).thenReturn(clientPage);
    when(clientBlockStatusRepository.findByClientEmail(client.getEmail()))
        .thenReturn(Optional.of(clientBlockStatus));
    when(mapper.map(client, ClientDisplayDto.class)).thenReturn(expectedDto);

    final Page<ClientDisplayDto> actualClientDto = clientService.getAllClients(pageable, null);

    verify(clientRepository, times(1)).findAll(pageable);
    verify(clientBlockStatusRepository, times(1)).findByClientEmail(client.getEmail());
    verify(mapper, times(1)).map(client, ClientDisplayDto.class);

    assertEquals(1, actualClientDto.getTotalElements());
    assertEquals(1, actualClientDto.getContent().size());
    assertEquals(expectedDto, actualClientDto.getContent().get(0));
  }

  @Nested
  class FindByEmail {

    @Test
    void testGetClientByEmailShouldReturnClient() {
      String email = "email";
      Client client = Client.builder().email(email).build();
      ClientDisplayDto expectedDto = ClientDisplayDto.builder().email(email).build();
      ClientBlockStatus clientBlockStatus = ClientBlockStatus.builder().isBlocked(false).build();

      when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
      when(clientBlockStatusRepository.findByClientEmail(email))
          .thenReturn(Optional.of(clientBlockStatus));
      when(mapper.map(client, ClientDisplayDto.class)).thenReturn(expectedDto);

      final ClientDisplayDto clientDisplayDto = clientService.getClientByEmail(email);

      verify(clientRepository, times(1)).findByEmail(email);
      verify(clientBlockStatusRepository, times(1)).findByClientEmail(email);
      verify(mapper, times(1)).map(client, ClientDisplayDto.class);

      assertEquals(expectedDto, clientDisplayDto);
    }

    @Test
    void testGetClientByEmailShouldThrowExceptionWhenClientNotFound() {
      String email = "email";
      String message = "Client with email: " + email + " not found";

      when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq(MessageKeys.ERROR_USER_NOT_FOUND), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(email));

      verify(clientRepository, times(1)).findByEmail(email);
      verify(mapper, never()).map(any(Client.class), any());
    }
  }

  @Nested
  class UpdateByEmail {

    @Test
    void testUpdateClientByEmailShouldReturnClient() {
      String email = "email";
      String oldName = "oldName";
      String newName = "newName";
      ClientUpdateDto dto = ClientUpdateDto.builder().name(newName).build();
      Client client = Client.builder().email(email).name(oldName).build();
      ClientDisplayDto expectedDto = ClientDisplayDto.builder().email(email).build();

      when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
      doNothing().when(mapper).map(dto, client);
      when(clientRepository.save(client)).thenReturn(client);
      when(mapper.map(client, ClientDisplayDto.class)).thenReturn(expectedDto);

      final ClientDisplayDto clientDisplayDto = clientService.updateClientByEmail(email, dto);

      verify(clientRepository, times(1)).findByEmail(email);
      verify(mapper, times(1)).map(dto, client);
      verify(clientRepository, times(1)).save(client);
      verify(mapper, times(1)).map(client, ClientDisplayDto.class);

      assertEquals(expectedDto, clientDisplayDto);
    }

    @Test
    void testUpdateClientByEmailShouldThrowExceptionWhenEmailNotFound() {
      String email = "email";
      ClientUpdateDto dto = ClientUpdateDto.builder().build();
      String message = "Client with email: " + email + " not found";

      when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq(MessageKeys.ERROR_USER_NOT_FOUND), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> clientService.updateClientByEmail(email, dto));

      verify(clientRepository, times(1)).findByEmail(email);
      verify(mapper, never()).map(any(ClientUpdateDto.class), any(Client.class));
      verify(clientRepository, never()).save(any(Client.class));
      verify(mapper, never()).map(any(Client.class), any());
    }
  }

  @Nested
  class DeleteByEmail {

    @Test
    void testDeleteClientByEmailShouldReturnNothing() {
      String email = "email";
      Client client = Client.builder().email(email).build();
      ClientBlockStatus clientBlockStatus = ClientBlockStatus.builder().isBlocked(false).build();

      when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
      doNothing().when(orderRepository).deleteAllByClientEmail(email);
      when(clientBlockStatusRepository.findByClientEmail(email))
          .thenReturn(Optional.of(clientBlockStatus));
      doNothing().when(clientRepository).delete(client);
      doNothing().when(clientBlockStatusRepository).delete(clientBlockStatus);

      clientService.deleteClientByEmail(email);

      verify(clientRepository, times(1)).findByEmail(email);
      verify(orderRepository, times(1)).deleteAllByClientEmail(email);
      verify(clientBlockStatusRepository, times(1)).findByClientEmail(email);
      verify(clientRepository, times(1)).delete(client);
      verify(clientBlockStatusRepository, times(1)).delete(clientBlockStatus);
    }

    @Test
    void testDeleteClientByEmailShouldThrowExceptionWhenEmailNotFound() {
      String email = "email";
      String message = "Client with email: " + email + " not found";

      when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq(MessageKeys.ERROR_USER_NOT_FOUND), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> clientService.deleteClientByEmail(email));

      verify(clientRepository, times(1)).findByEmail(email);
      verify(clientRepository, never()).delete(any(Client.class));
    }
  }

  @Nested
  class ChangePassword {

    @Test
    void testChangePasswordShouldReturn() {
      String email = "test@test.com";
      String oldPassword = "oldPassword";
      String newPassword = "newPassword";
      final ChangePasswordDto dto = ChangePasswordDto.builder().oldPassword(oldPassword)
          .newPassword(newPassword).build();
      Client client = Client.builder().email(email).password(oldPassword).build();

      when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
      when(passwordEncoder.matches(oldPassword, client.getPassword())).thenReturn(true);
      when(passwordEncoder.encode(newPassword)).thenReturn(newPassword);
      when(clientRepository.save(client)).thenReturn(client);

      clientService.changePassword(email, dto);

      verify(clientRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, times(1)).matches(oldPassword, oldPassword);
      verify(passwordEncoder, times(1)).encode(newPassword);
      verify(clientRepository, times(1)).save(client);
    }

    @Test
    void testChangePasswordShouldThrowExceptionWhenEmailNotFound() {
      String email = "test@test.com";
      ChangePasswordDto dto = ChangePasswordDto.builder().build();
      String message = "Client with email: " + email + " not found";

      when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq(MessageKeys.ERROR_USER_NOT_FOUND), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> clientService.changePassword(email, dto));

      verify(clientRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
      verify(passwordEncoder, never()).encode(any(String.class));
      verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testChangePasswordShouldThrowExceptionWhenOldPasswordInvalid() {
      String email = "test@test.com";
      String passwordDto = "oldPassword";
      String passwordClient = "";
      final ChangePasswordDto dto = ChangePasswordDto.builder().oldPassword(passwordDto).build();
      Client client = Client.builder().email(email).password(passwordClient).build();
      String message = "Invalid old password";

      when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
      when(passwordEncoder.matches(passwordDto, client.getPassword())).thenReturn(false);
      when(messageSource.getMessage(
          eq(MessageKeys.ERROR_USER_OLD_PASSWORD_NOT_MATCH), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(InvalidPasswordException.class, () -> clientService.changePassword(email, dto));

      verify(clientRepository, times(1)).findByEmail(email);
      verify(passwordEncoder, times(1)).matches(passwordDto, passwordClient);
      verify(passwordEncoder, never()).encode(any(String.class));
      verify(clientRepository, never()).save(any(Client.class));
    }
  }

  @Nested
  class AddClient {

    @Test
    void testAddClientShouldReturnClient() {
      String email = "test@test.com";
      ClientCreateDto dto = ClientCreateDto.builder().email(email).build();
      Client client = Client.builder().email(email).build();
      ClientDisplayDto expectedDto = ClientDisplayDto.builder().email(email).build();
      ClientBlockStatus clientBlockStatus = ClientBlockStatus.builder().isBlocked(false).build();

      when(clientRepository.existsByEmail(email)).thenReturn(false);
      when(employeeRepository.existsByEmail(email)).thenReturn(false);
      when(clientBlockStatusRepository.existsByClientEmail(email)).thenReturn(false);
      when(mapper.map(dto, Client.class)).thenReturn(client);
      when(clientRepository.save(client)).thenReturn(client);
      when(clientBlockStatusRepository.save(any(ClientBlockStatus.class)))
          .thenReturn(clientBlockStatus);
      when(clientBlockStatusRepository.findByClientEmail(email))
          .thenReturn(Optional.of(clientBlockStatus));
      when(mapper.map(client, ClientDisplayDto.class)).thenReturn(expectedDto);

      final ClientDisplayDto actualClientDto = clientService.addClient(dto);

      verify(clientRepository, times(1)).existsByEmail(email);
      verify(employeeRepository, times(1)).existsByEmail(email);
      verify(clientBlockStatusRepository, times(1)).existsByClientEmail(email);
      verify(mapper, times(1)).map(dto, Client.class);
      verify(clientRepository, times(1)).save(client);
      verify(clientBlockStatusRepository, times(1)).save(any(ClientBlockStatus.class));
      verify(clientBlockStatusRepository, times(1)).findByClientEmail(email);
      verify(mapper, times(1)).map(client, ClientDisplayDto.class);

      assertEquals(expectedDto, actualClientDto);
    }

    @Test
    void testAddClientShouldThrowExceptionWhenClientEmailAlreadyExist() {
      String email = "test@test.com";
      ClientCreateDto dto = ClientCreateDto.builder().email(email).build();
      String message = "Client with email: " + email + " already exist";

      when(clientRepository.existsByEmail(email)).thenReturn(true);
      when(messageSource.getMessage(
          eq(MessageKeys.ERROR_USER_ALREADY_EXISTS), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));

      verify(clientRepository, times(1)).existsByEmail(email);
      verify(employeeRepository, never()).existsByEmail(anyString());
      verify(mapper, never()).map(any(ClientCreateDto.class), any());
      verify(clientRepository, never()).save(any(Client.class));
      verify(mapper, never()).map(any(Client.class), any());
    }

    @Test
    void testAddClientShouldThrowExceptionWhenEmployeeEmailAlreadyExist() {
      String email = "test@test.com";
      final ClientCreateDto dto = ClientCreateDto.builder().email(email).build();
      String message = "Employee with email: " + email + " already exist";

      when(clientRepository.existsByEmail(email)).thenReturn(false);
      when(employeeRepository.existsByEmail(email)).thenReturn(true);
      when(messageSource.getMessage(
          eq(MessageKeys.ERROR_USER_ALREADY_EXISTS), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));

      verify(clientRepository, times(1)).existsByEmail(email);
      verify(employeeRepository, times(1)).existsByEmail(anyString());
      verify(mapper, never()).map(any(ClientCreateDto.class), any());
      verify(clientRepository, never()).save(any(Client.class));
      verify(mapper, never()).map(any(Client.class), any());
    }
  }

  @Nested
  class AddBalanceToClient {

    @Test
    void testAddBalanceToClientShouldReturnClient() {
      String email = "test@test.com";
      BigDecimal amount = BigDecimal.TEN;
      AddBalanceDto dto = AddBalanceDto.builder().amount(amount).build();
      Client client = Client.builder().email(email).balance(BigDecimal.ZERO).build();
      ClientDisplayDto expectedDto = ClientDisplayDto.builder().email(email)
          .balance(amount).build();
      ClientBlockStatus clientBlockStatus = ClientBlockStatus.builder().isBlocked(false).build();

      when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
      when(clientRepository.save(client)).thenReturn(client);
      when(clientBlockStatusRepository.findByClientEmail(email))
          .thenReturn(Optional.of(clientBlockStatus));
      when(mapper.map(client, ClientDisplayDto.class)).thenReturn(expectedDto);

      final ClientDisplayDto actualClientDto = clientService.addBalanceToClient(email, dto);

      verify(clientRepository, times(1)).findByEmail(email);
      verify(clientRepository, times(1)).save(client);
      verify(clientBlockStatusRepository, times(1)).findByClientEmail(email);
      verify(mapper, times(1)).map(client, ClientDisplayDto.class);

      assertEquals(expectedDto, actualClientDto);
    }

    @Test
    void testAddBalanceToClientShouldThrowExceptionWhenEmailNotFound() {
      String email = "test@test.com";
      AddBalanceDto dto = AddBalanceDto.builder().build();
      String message = "Client with email: " + email + " not found";

      when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq(MessageKeys.ERROR_USER_NOT_FOUND), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> clientService.addBalanceToClient(email, dto));

      verify(clientRepository, times(1)).findByEmail(email);
      verify(clientRepository, never()).save(any(Client.class));
      verify(mapper, never()).map(any(Client.class), any());
    }
  }

  @Nested
  class BlockClient {

    @Test
    void testBlockClientShouldDoNothing() {
      String email = "test@test.com";
      ClientBlockStatus clientBlockStatus = ClientBlockStatus.builder().isBlocked(false).build();

      when(clientBlockStatusRepository.findByClientEmail(email))
          .thenReturn(Optional.of(clientBlockStatus));
      when(clientBlockStatusRepository.save(clientBlockStatus)).thenReturn(clientBlockStatus);

      clientService.blockClient(email);

      verify(clientBlockStatusRepository, times(1)).findByClientEmail(email);
      verify(clientBlockStatusRepository, times(1)).save(clientBlockStatus);
    }

    @Test
    void testBlockClientShouldThrowExceptionWhenEmailNotFound() {
      String email = "test@test.com";
      String message = "Client with email: " + email + " not found";

      when(clientBlockStatusRepository.findByClientEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq(MessageKeys.ERROR_USER_NOT_FOUND), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> clientService.blockClient(email));

      verify(clientBlockStatusRepository, times(1)).findByClientEmail(email);
      verify(clientBlockStatusRepository, never()).save(any(ClientBlockStatus.class));
    }
  }

  @Nested
  class UnblockClient {

    @Test
    void testBlockClientShouldDoNothing() {
      String email = "test@test.com";
      ClientBlockStatus clientBlockStatus = ClientBlockStatus.builder().isBlocked(false).build();

      when(clientBlockStatusRepository.findByClientEmail(email))
          .thenReturn(Optional.of(clientBlockStatus));
      when(clientBlockStatusRepository.save(clientBlockStatus)).thenReturn(clientBlockStatus);

      clientService.unblockClient(email);

      verify(clientBlockStatusRepository, times(1)).findByClientEmail(email);
      verify(clientBlockStatusRepository, times(1)).save(clientBlockStatus);
    }

    @Test
    void testBlockClientShouldThrowExceptionWhenEmailNotFound() {
      String email = "test@test.com";
      String message = "Client with email: " + email + " not found";

      when(clientBlockStatusRepository.findByClientEmail(email)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq(MessageKeys.ERROR_USER_NOT_FOUND), any(), any(Locale.class)))
          .thenReturn(message);

      assertThrows(NotFoundException.class, () -> clientService.unblockClient(email));

      verify(clientBlockStatusRepository, times(1)).findByClientEmail(email);
      verify(clientBlockStatusRepository, never()).save(any(ClientBlockStatus.class));
    }
  }
}
