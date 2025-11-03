package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.InvalidPasswordException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @InjectMocks
    private ClientServiceImpl clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ModelMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void testGetAllClients_ShouldReturnPagedClients() {
        Client client = Client.builder().build();
        ClientDisplayDTO expectedDto = new ClientDisplayDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(Arrays.asList(client), pageable, 1);

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);
        when(mapper.map(client, ClientDisplayDTO.class)).thenReturn(expectedDto);

        Page<ClientDisplayDTO> actualClientDto = clientService.getAllClients(pageable);

        verify(clientRepository, times(1)).findAll(pageable);
        verify(mapper, times(1)).map(client, ClientDisplayDTO.class);

        assertEquals(1, actualClientDto.getTotalElements());
        assertEquals(1, actualClientDto.getContent().size());
        assertEquals(expectedDto, actualClientDto.getContent().get(0));
    }

    @Nested
    class FindByEmail {

        @Test
        void testGetClientByEmail_ShouldReturnClient() {
            String email = "email";
            Client client = Client.builder().email(email).build();
            ClientDisplayDTO expectedDto = ClientDisplayDTO.builder().email(email).build();

            when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
            when(mapper.map(client, ClientDisplayDTO.class)).thenReturn(expectedDto);

            ClientDisplayDTO clientDisplayDTO = clientService.getClientByEmail(email);

            verify(clientRepository, times(1)).findByEmail(email);
            verify(mapper, times(1)).map(client, ClientDisplayDTO.class);

            assertEquals(expectedDto, clientDisplayDTO);
        }

        @Test
        void testGetClientByEmail_ShouldThrowExceptionWhenClientNotFound() {
            String email = "email";

            when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(email));

            verify(clientRepository, times(1)).findByEmail(email);
            verify(mapper, never()).map(any(Client.class), any());
        }
    }

    @Nested
    class UpdateByEmail {

        @Test
        void testUpdateClientByEmail_ShouldReturnClient() {
            String email = "email";
            String oldName = "oldName";
            String newName = "newName";
            ClientUpdateDTO dto = ClientUpdateDTO.builder().email(email).name(newName).build();
            Client client = Client.builder().email(email).name(oldName).build();
            ClientDisplayDTO expectedDto = ClientDisplayDTO.builder().email(email).build();

            when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
            doNothing().when(mapper).map(dto, client);
            when(clientRepository.save(client)).thenReturn(client);
            when(mapper.map(client, ClientDisplayDTO.class)).thenReturn(expectedDto);

            ClientDisplayDTO clientDisplayDTO = clientService.updateClientByEmail(email, dto);

            verify(clientRepository, times(1)).findByEmail(email);
            verify(mapper, times(1)).map(dto, client);
            verify(clientRepository, times(1)).save(client);
            verify(mapper, times(1)).map(client, ClientDisplayDTO.class);

            assertEquals(expectedDto, clientDisplayDTO);
        }

        @Test
        void testUpdateClientByEmail_ShouldThrowExceptionWhenEmailNotFound() {
            String email = "email";
            ClientUpdateDTO dto = ClientUpdateDTO.builder().email(email).build();

            when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> clientService.updateClientByEmail(email, dto));

            verify(clientRepository, times(1)).findByEmail(email);
            verify(mapper, never()).map(any(ClientUpdateDTO.class), any(Client.class));
            verify(clientRepository, never()).save(any(Client.class));
            verify(mapper, never()).map(any(Client.class), any());
        }

        @Test
        void testUpdateClientByEmail_ShouldThrowExceptionWhenEmailAlreadyExist() {
            String oldEmail = "oldEmail";
            String newEmail = "newEmail";
            ClientUpdateDTO dto = ClientUpdateDTO.builder().email(newEmail).build();
            Client client = Client.builder().email(oldEmail).build();

            when(clientRepository.findByEmail(oldEmail)).thenReturn(Optional.of(client));
            when(clientRepository.existsByEmail(newEmail)).thenReturn(true);

            assertThrows(AlreadyExistException.class, () -> clientService.updateClientByEmail(oldEmail, dto));

            verify(clientRepository, times(1)).findByEmail(oldEmail);
            verify(clientRepository, times(1)).existsByEmail(newEmail);
            verify(mapper, never()).map(any(ClientUpdateDTO.class), any(Client.class));
            verify(clientRepository, never()).save(any(Client.class));
            verify(mapper, never()).map(any(Client.class), any());
        }
    }

    @Nested
    class DeleteByEmail {

        @Test
        void testDeleteClientByEmail_ShouldReturnNothing() {
            String email = "email";
            Client client = Client.builder().email(email).build();

            when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
            doNothing().when(clientRepository).delete(client);

            clientService.deleteClientByEmail(email);

            verify(clientRepository, times(1)).findByEmail(email);
            verify(clientRepository, times(1)).delete(client);
        }

        @Test
        void testDeleteClientByEmail_ShouldThrowExceptionWhenEmailNotFound() {
            String email = "email";

            when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> clientService.deleteClientByEmail(email));

            verify(clientRepository, times(1)).findByEmail(email);
            verify(clientRepository, never()).delete(any(Client.class));
        }
    }

    @Nested
    class ChangePassword {

        @Test
        void testChangePassword_ShouldReturn() {
            String email = "test@test.com";
            String oldPassword = "oldPassword";
            String newPassword = "newPassword";
            ChangePasswordDTO dto = ChangePasswordDTO.builder().oldPassword(oldPassword).newPassword(newPassword).build();
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
        void testChangePassword_ShouldThrowExceptionWhenEmailNotFound() {
            String email = "test@test.com";
            ChangePasswordDTO dto = ChangePasswordDTO.builder().build();

            when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> clientService.changePassword(email, dto));

            verify(clientRepository, times(1)).findByEmail(email);
            verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
            verify(passwordEncoder, never()).encode(any(String.class));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        void testChangePassword_ShouldThrowExceptionWhenOldPasswordInvalid() {
            String email = "test@test.com";
            String passwordDto = "oldPassword";
            String passwordClient = "";
            ChangePasswordDTO dto = ChangePasswordDTO.builder().oldPassword(passwordDto).build();
            Client client = Client.builder().email(email).password(passwordClient).build();

            when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
            when(passwordEncoder.matches(passwordDto, client.getPassword())).thenReturn(false);

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
        void testAddClient_ShouldReturnClient() {
            String email = "test@test.com";
            ClientCreateDTO dto = ClientCreateDTO.builder().email(email).build();
            Client client = Client.builder().email(email).build();
            ClientDisplayDTO expectedDto = ClientDisplayDTO.builder().email(email).build();

            when(clientRepository.existsByEmail(email)).thenReturn(false);
            when(mapper.map(dto, Client.class)).thenReturn(client);
            when(clientRepository.save(client)).thenReturn(client);
            when(mapper.map(client, ClientDisplayDTO.class)).thenReturn(expectedDto);

            ClientDisplayDTO actualClientDto = clientService.addClient(dto);

            verify(clientRepository, times(1)).existsByEmail(email);
            verify(mapper, times(1)).map(dto, Client.class);
            verify(clientRepository, times(1)).save(client);
            verify(mapper, times(1)).map(client, ClientDisplayDTO.class);

            assertEquals(expectedDto, actualClientDto);
        }

        @Test
        void testAddClient_ShouldThrowExceptionWhenEmailAlreadyExist() {
            String email = "test@test.com";
            ClientCreateDTO dto = ClientCreateDTO.builder().email(email).build();

            when(clientRepository.existsByEmail(email)).thenReturn(true);

            assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));

            verify(clientRepository, times(1)).existsByEmail(email);
            verify(mapper, never()).map(any(ClientCreateDTO.class), any());
            verify(clientRepository, never()).save(any(Client.class));
            verify(mapper, never()).map(any(Client.class), any());
        }
    }

    @Nested
    class AddBalanceToClient {

        @Test
        void testAddBalanceToClient_ShouldReturnClient() {
            String email = "test@test.com";
            BigDecimal amount = BigDecimal.TEN;
            AddBalanceDTO dto = AddBalanceDTO.builder().amount(amount).build();
            Client client = Client.builder().email(email).balance(BigDecimal.ZERO).build();
            ClientDisplayDTO expectedDto = ClientDisplayDTO.builder().email(email).balance(amount).build();

            when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);
            when(mapper.map(client, ClientDisplayDTO.class)).thenReturn(expectedDto);

            ClientDisplayDTO actualClientDto = clientService.addBalanceToClient(email, dto);

            verify(clientRepository, times(1)).findByEmail(email);
            verify(clientRepository, times(1)).save(client);
            verify(mapper, times(1)).map(client, ClientDisplayDTO.class);

            assertEquals(expectedDto, actualClientDto);
        }

        @Test
        void testAddBalanceToClient_ShouldThrowExceptionWhenEmailNotFound() {
            String email = "test@test.com";
            AddBalanceDTO dto = AddBalanceDTO.builder().build();

            when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> clientService.addBalanceToClient(email, dto));

            verify(clientRepository, times(1)).findByEmail(email);
            verify(clientRepository, never()).save(any(Client.class));
            verify(mapper, never()).map(any(Client.class), any());
        }
    }
}
