package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
            mockMvc.perform(get("/clients"))
                    .andExpect(status().isOk());
        }
    }
}
