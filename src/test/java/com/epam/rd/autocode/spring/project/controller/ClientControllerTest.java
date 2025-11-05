package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(ClientController.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Nested
    class GetClients {

        @Test
        void testGetAllClients_ShouldReturnClientsList() throws Exception {
            Page<ClientDisplayDTO> clientPage = new PageImpl<>(Collections.singletonList(new ClientDisplayDTO()));
            when(clientService.getAllClients(any(Pageable.class))).thenReturn(clientPage);

            mockMvc.perform(get("/clients")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name")
                            .with(user("testuser").roles("EMPLOYEE")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("clients"))
                    .andExpect(model().attribute("clientPage", clientPage));
        }
    }

    @Nested
    class GetClientByEmail {

        @Test
        void testGetClient_ShouldReturnClient_WhenSuccess() throws Exception {
            String email = "a";
            ClientDisplayDTO clientDto = ClientDisplayDTO.builder().email(email).build();

            when(clientService.getClientByEmail(email)).thenReturn(clientDto);

            mockMvc.perform(get("/clients/{email}", email)
                            .with(user("testuser").roles("EMPLOYEE")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("client-detail"))
                    .andExpect(model().attribute("client", clientDto));
        }
    }

    @Nested
    class UpdateClient {

        @Test
        void testUpdateClient_ShouldRedirectToProfile_WhenSuccess() throws Exception {
            String email = "test@test.com";
            String name = "name";
            ClientUpdateDTO clientUpdateDTO = ClientUpdateDTO.builder()
                    .name(name)
                    .build();
            ClientDisplayDTO clientDisplayDTO = new ClientDisplayDTO();

            when(clientService.updateClientByEmail(eq(email), any(ClientUpdateDTO.class))).thenReturn(clientDisplayDTO);

            mockMvc.perform(put("/clients/profile")
                            .flashAttr("clientUpdateDTO", clientUpdateDTO)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"))
                    .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        void testUpdateClient_ShouldRedirectToProfile_WhenValidationFails() throws Exception {
            String email = "test@test.com";
            ClientUpdateDTO clientUpdateDTO = ClientUpdateDTO.builder()
                    .build();
            ClientDisplayDTO clientDisplayDTO = new ClientDisplayDTO();

            when(clientService.updateClientByEmail(eq(email), any(ClientUpdateDTO.class))).thenReturn(clientDisplayDTO);

            mockMvc.perform(put("/clients/profile")
                            .flashAttr("clientUpdateDTO", clientUpdateDTO)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?error=validation"))
                    .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.clientUpdateDTO"))
                    .andExpect(flash().attributeExists("clientUpdateDTO"));
        }
    }
}
