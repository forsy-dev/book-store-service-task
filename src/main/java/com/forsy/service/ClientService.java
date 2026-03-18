package com.forsy.service;

import com.forsy.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    Page<ClientDisplayDTO> getAllClients(Pageable pageable, String keyword);

    ClientDisplayDTO getClientByEmail(String email);

    ClientDisplayDTO updateClientByEmail(String email, ClientUpdateDTO client);

    void deleteClientByEmail(String email);

    ClientDisplayDTO addClient(ClientCreateDTO client);

    void changePassword(String email, ChangePasswordDTO dto);

    ClientDisplayDTO addBalanceToClient(String email, AddBalanceDTO dto);

    void blockClient(String email);

    void unblockClient(String email);
}
