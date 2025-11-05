package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Test
    void testGetClients_WhenAnonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Nested
    class GetClients {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetClients_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            mockMvc.perform(get("/clients"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetClients_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            Page<ClientDisplayDTO> clientPage = new PageImpl<>(Collections.singletonList(new ClientDisplayDTO()));

            when(clientService.getAllClients(any(Pageable.class))).thenReturn(clientPage);

            mockMvc.perform(get("/clients"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class GetClientByEmail {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetClientByEmail_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            String email = "email";

            mockMvc.perform(get("/clients/{email}", email))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetClientByEmail_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            String email = "email";

            when(clientService.getClientByEmail(email)).thenReturn(new ClientDisplayDTO());

            mockMvc.perform(get("/clients/{email}", email))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class UpdateClient {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testUpdateClient_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {
            String email = "test@test.com";
            String name = "name";
            ClientUpdateDTO clientUpdateDTO = ClientUpdateDTO.builder()
                    .name(name)
                    .build();
            ClientDisplayDTO clientDisplayDTO = new ClientDisplayDTO();

            when(clientService.updateClientByEmail(eq(email), any(ClientUpdateDTO.class))).thenReturn(clientDisplayDTO);

            mockMvc.perform(put("/clients/profile")
                            .flashAttr("clientUpdateDTO", clientUpdateDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testUpdateClient_WhenAuthenticatedAsEmployee_ShouldForbidAccess() throws Exception {

            mockMvc.perform(put("/clients/profile"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteClient {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testDeleteClient_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {
            String email = "test@test.com";

            doNothing().when(clientService).deleteClientByEmail(email);

            mockMvc.perform(delete("/clients/profile"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?accountDeleted=true"));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testDeleteClient_WhenAuthenticatedAsEmployee_ShouldForbidAccess() throws Exception {

            mockMvc.perform(delete("/clients/profile"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class BlockClient {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testBlockClient_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            String email = "test@test.com";

            mockMvc.perform(put("/clients/{email}/block", email))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testBlockClient_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            String email = "test@test.com";

            doNothing().when(clientService).blockClient(email);

            mockMvc.perform(put("/clients/{email}/block", email))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/clients/" + email));
        }
    }

    @Nested
    class UnblockClient {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testUnblockClient_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            String email = "test@test.com";

            mockMvc.perform(put("/clients/{email}/unblock", email))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testUnblockClient_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            String email = "test@test.com";

            doNothing().when(clientService).unblockClient(email);

            mockMvc.perform(put("/clients/{email}/unblock", email))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/clients/" + email));
        }
    }
}
