package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
