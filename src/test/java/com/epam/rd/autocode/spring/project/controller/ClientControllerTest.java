package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.jwt.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.AddBalanceDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.util.CartCookieUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(ClientController.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private CartCookieUtil cartCookieUtil;

    @Nested
    class GetClients {

        @Test
        void testGetAllClients_ShouldReturnClientsList() throws Exception {
            Page<ClientDisplayDTO> clientPage = new PageImpl<>(Collections.singletonList(new ClientDisplayDTO()));
            when(clientService.getAllClients(any(Pageable.class), nullable(String.class))).thenReturn(clientPage);

            mockMvc.perform(get("/clients")
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

    @Nested
    class DeleteClient {

        @Test
        void testDeleteClient_ShouldRedirectToLogin_WhenSuccess() throws Exception {
            String email = "test@test.com";

            doNothing().when(clientService).deleteClientByEmail(email);

            mockMvc.perform(delete("/clients/profile")
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?accountDeleted=true"));
        }

        @Test
        void testDeleteClient_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
            String email = "test@test.com";

            doThrow(new NotFoundException("Client not found")).when(clientService).deleteClientByEmail(email);

            mockMvc.perform(delete("/clients/profile")
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class BlockClient {

        @Test
        void testBlockClient_ShouldRedirectToClientDetail_WhenSuccess() throws Exception {
            String email = "test@test.com";

            doNothing().when(clientService).blockClient(email);

            mockMvc.perform(put("/clients/{email}/block", email)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/clients/" + email));
        }

        @Test
        void testBlockClient_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
            String email = "test@test.com";

            doThrow(NotFoundException.class).when(clientService).blockClient(email);

            mockMvc.perform(put("/clients/{email}/block", email)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UnblockClient {

        @Test
        void testUnblockClient_ShouldRedirectToClientDetail_WhenSuccess() throws Exception {
            String email = "test@test.com";

            doNothing().when(clientService).unblockClient(email);

            mockMvc.perform(put("/clients/{email}/unblock", email)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/clients/" + email));
        }

        @Test
        void testUnblockClient_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
            String email = "test@test.com";

            doThrow(NotFoundException.class).when(clientService).unblockClient(email);

            mockMvc.perform(put("/clients/{email}/unblock", email)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class AddBalanceToClient {

        @Test
        void testAddBalanceToClient_ShouldRedirectToClientDetail_WhenSuccess() throws Exception {
            String clientEmail = "test@test.com";
            String employeeEmail = "test@emp.com";
            AddBalanceDTO dto = AddBalanceDTO.builder().amount(BigDecimal.TEN).build();
            ClientDisplayDTO clientDisplayDTO = ClientDisplayDTO.builder().email(clientEmail).balance(BigDecimal.TEN).build();

            when(clientService.addBalanceToClient(clientEmail, dto)).thenReturn(clientDisplayDTO);

            mockMvc.perform(post("/clients/{email}/add-balance", clientEmail)
                    .with(user(employeeEmail).roles("EMPLOYEE"))
                    .flashAttr("addBalanceDTO", dto)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/" + clientEmail))
                .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        void testAddBalanceToClient_ShouldRedirectToClientDetail_WhenValidationFails() throws Exception {
            String clientEmail = "test@test.com";
            String employeeEmail = "test@emp.com";
            AddBalanceDTO dto = AddBalanceDTO.builder().amount(BigDecimal.ZERO).build();
            ClientDisplayDTO clientDisplayDTO = ClientDisplayDTO.builder().email(clientEmail).balance(BigDecimal.ZERO).build();

            when(clientService.addBalanceToClient(clientEmail, dto)).thenReturn(clientDisplayDTO);

            mockMvc.perform(post("/clients/{email}/add-balance", clientEmail)
                    .with(user(employeeEmail).roles("EMPLOYEE"))
                    .flashAttr("addBalanceDTO", dto)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/" + clientEmail))
                .andExpect(flash().attributeExists("errorMessage"));
        }

        @Test
        void testAddBalanceToClient_ShouldRedirectToClientDetail_WhenAddingBalanceToClientFails() throws Exception {
            String clientEmail = "test@test.com";
            String employeeEmail = "test@emp.com";
            AddBalanceDTO dto = AddBalanceDTO.builder().amount(BigDecimal.ZERO).build();

            when(clientService.addBalanceToClient(clientEmail, dto)).thenThrow(new RuntimeException());

            mockMvc.perform(post("/clients/{email}/add-balance", clientEmail)
                    .with(user(employeeEmail).roles("EMPLOYEE"))
                    .flashAttr("addBalanceDTO", dto)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/" + clientEmail))
                .andExpect(flash().attributeExists("errorMessage"));
        }
    }
}
