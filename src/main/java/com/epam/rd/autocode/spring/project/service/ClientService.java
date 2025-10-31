package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {

    Page<ClientDisplayDTO> getAllClients(Pageable pageable);

    ClientDisplayDTO getClientByEmail(String email);

    ClientDisplayDTO updateClientByEmail(String email, ClientDTO client);

    void deleteClientByEmail(String email);

    ClientDisplayDTO addClient(ClientDTO client);
}
