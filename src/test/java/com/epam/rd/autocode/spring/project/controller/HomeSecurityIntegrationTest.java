package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientCreateDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

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

    @Nested
    class RegisterClient {

        ClientCreateDTO clientCreateDTO;
        ClientDisplayDTO clientDisplayDTO;

        @BeforeEach
        void setUp() {
            clientCreateDTO = ClientCreateDTO.builder()
                    .name("testclient")
                    .email("test@test.com")
                    .password("Te$t1234")
                    .build();
            clientDisplayDTO = ClientDisplayDTO.builder()
                    .name("testclient")
                    .email("test@test.com")
                    .build();
        }

        @Test
        void testRegisterClient_WhenAnonymous_ShouldRedirectToLoginPage() throws Exception {

            when(clientService.addClient(any(ClientCreateDTO.class))).thenReturn(clientDisplayDTO);

            mockMvc.perform(post("/register")
                            .flashAttr("client", clientCreateDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void testRegisterClient_WhenAuthorizedAsClient_ShouldRedirectToBooksPage() throws Exception {

            mockMvc.perform(post("/register")
                            .flashAttr("client", clientCreateDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testRegisterClient_WhenAuthorizedAsEmployee_ShouldRedirectToBooksPage() throws Exception {

            mockMvc.perform(post("/register")
                            .flashAttr("client", clientCreateDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }
    }
}
