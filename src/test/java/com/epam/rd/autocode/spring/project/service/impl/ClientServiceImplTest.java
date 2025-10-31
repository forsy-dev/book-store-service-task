package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void testGetClientByEmail_ShouldReturnClient() {
        String email = "email";
        Client client = Client.builder().email(email).build();
        ClientDisplayDTO expectedDto = ClientDisplayDTO.builder().email(email).build();

        when(clientRepository.findByEmail(email)).thenReturn(client);
        when(mapper.map(client, ClientDisplayDTO.class)).thenReturn(expectedDto);

        ClientDisplayDTO clientDisplayDTO = clientService.getClientByEmail(email);

        verify(clientRepository, times(1)).findByEmail(email);
        verify(mapper, times(1)).map(client, ClientDisplayDTO.class);

        assertEquals(expectedDto, clientDisplayDTO);
    }
}
