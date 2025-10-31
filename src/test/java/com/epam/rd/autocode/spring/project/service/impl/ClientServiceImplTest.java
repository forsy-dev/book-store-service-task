package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
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

import java.rmi.AlreadyBoundException;
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
}
