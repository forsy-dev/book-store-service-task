package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientCreateDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HomeController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Test
    void testGetLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testGetRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register-form"));
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
        void testRegisterClient_ShouldRedirect() throws Exception {

            when(clientService.addClient(any(ClientCreateDTO.class))).thenReturn(clientDisplayDTO);

            mockMvc.perform(post("/register")
                            .flashAttr("client", clientCreateDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        void testRegisterClient_ShouldReturnToRegisterForm_WhenValidationFails() throws Exception {
            clientCreateDTO.setPassword("");

            when(clientService.addClient(any(ClientCreateDTO.class))).thenReturn(clientDisplayDTO);

            mockMvc.perform(post("/register")
                            .flashAttr("client", clientCreateDTO))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register-form"));
        }

        @Test
        void testRegisterClient_ShouldReturnToRegisterForm_WhenEmailAlreadyExist() throws Exception {
            when(clientService.addClient(any(ClientCreateDTO.class))).thenThrow(new AlreadyExistException("Client already exists"));

            mockMvc.perform(post("/register")
                            .flashAttr("client", clientCreateDTO))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register-form"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }
}
