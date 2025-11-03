package com.epam.rd.autocode.spring.project.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetLoginPage_WhenAnonymous_ShouldReturnLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Nested
    class GetRegisterPage {

        @Test
        void testGetRegisterPage_WhenAnonymous_ShouldReturnRegisterPage() throws Exception {
            mockMvc.perform(get("/register"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetRegisterPage_WhenAuthenticatedAsClient_ShouldRedirect() throws Exception {
            mockMvc.perform(get("/register"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetRegisterPage_WhenAuthenticatedAsEmployee_ShouldRedirect() throws Exception {
            mockMvc.perform(get("/register"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }
    }
}
