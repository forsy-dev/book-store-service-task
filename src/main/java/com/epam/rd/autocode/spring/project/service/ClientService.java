package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    Page<ClientDisplayDTO> getAllClients(Pageable pageable);

    ClientDisplayDTO getClientByEmail(String email);

    ClientDisplayDTO updateClientByEmail(String email, ClientUpdateDTO client);

    void deleteClientByEmail(String email);

    ClientDisplayDTO addClient(ClientCreateDTO client);

    void changePassword(String email, ChangePasswordDTO dto);

    ClientDisplayDTO addBalanceToClient(String email, AddBalanceDTO dto);
}
