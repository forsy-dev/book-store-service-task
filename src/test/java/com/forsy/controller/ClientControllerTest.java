package com.forsy.controller;

import com.forsy.conf.jwt.JwtUtils;
import com.forsy.dto.AddBalanceDTO;
import com.forsy.dto.ClientDisplayDTO;
import com.forsy.dto.ClientUpdateDTO;
import com.forsy.exception.NotFoundException;
import com.forsy.service.ClientService;
import com.forsy.util.CartCookieUtil;
import com.forsy.util.WebConstants;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

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

            mockMvc.perform(get(WebConstants.URL_CLIENTS)
                            .with(user("testuser").roles("EMPLOYEE")))
                    .andExpect(status().isOk())
                    .andExpect(view().name(WebConstants.VIEW_CLIENTS))
                    .andExpect(model().attribute(WebConstants.ATTR_CLIENT_PAGE, clientPage));
        }
    }

    @Nested
    class GetClientByEmail {

        @Test
        void testGetClient_ShouldReturnClient_WhenSuccess() throws Exception {
            String email = "a";
            ClientDisplayDTO clientDto = ClientDisplayDTO.builder().email(email).build();

            when(clientService.getClientByEmail(email)).thenReturn(clientDto);
            mockMvc.perform(get(WebConstants.URL_CLIENT_DETAIL, email)
                            .with(user("testuser").roles("EMPLOYEE")))
                    .andExpect(status().isOk())
                    .andExpect(view().name(WebConstants.VIEW_CLIENT_DETAIL))
                    .andExpect(model().attribute(WebConstants.ATTR_CLIENT, clientDto));
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

            mockMvc.perform(put(WebConstants.URL_CLIENT_PROFILE)
                            .flashAttr(WebConstants.ATTR_CLIENT_UPDATE_DTO, clientUpdateDTO)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(WebConstants.URL_PROFILE))
                    .andExpect(flash().attributeExists(WebConstants.ATTR_SUCCESS_MESSAGE));
        }

        @Test
        void testUpdateClient_ShouldRedirectToProfile_WhenValidationFails() throws Exception {
            String email = "test@test.com";
            ClientUpdateDTO clientUpdateDTO = ClientUpdateDTO.builder()
                    .build();

            mockMvc.perform(put(WebConstants.URL_CLIENT_PROFILE)
                            .flashAttr(WebConstants.ATTR_CLIENT_UPDATE_DTO, clientUpdateDTO)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(WebConstants.addParameters(WebConstants.URL_PROFILE, Map.of("error", "validation"))))
                    .andExpect(flash().attributeExists(WebConstants.getBindingResultKey(WebConstants.ATTR_CLIENT_UPDATE_DTO)))
                    .andExpect(flash().attributeExists(WebConstants.ATTR_CLIENT_UPDATE_DTO));
        }
    }

    @Nested
    class DeleteClient {

        @Test
        void testDeleteClient_ShouldRedirectToLogin_WhenSuccess() throws Exception {
            String email = "test@test.com";

            doNothing().when(clientService).deleteClientByEmail(email);

            mockMvc.perform(delete(WebConstants.URL_CLIENT_PROFILE)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(WebConstants.addParameters(WebConstants.URL_LOGIN, Map.of("accountDeleted", "true"))));
        }

        @Test
        void testDeleteClient_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
            String email = "test@test.com";

            doThrow(new NotFoundException("Client not found")).when(clientService).deleteClientByEmail(email);

            mockMvc.perform(delete(WebConstants.URL_CLIENT_PROFILE)
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

            mockMvc.perform(put(WebConstants.URL_CLIENT_BLOCK, email)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, email)));
        }

        @Test
        void testBlockClient_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
            String email = "test@test.com";

            doThrow(NotFoundException.class).when(clientService).blockClient(email);

            mockMvc.perform(put(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_BLOCK, email))
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

            mockMvc.perform(put(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_UNBLOCK, email))
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, email)));
        }

        @Test
        void testUnblockClient_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
            String email = "test@test.com";

            doThrow(NotFoundException.class).when(clientService).unblockClient(email);

            mockMvc.perform(put(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_UNBLOCK, email))
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

            mockMvc.perform(post(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_ADD_BALANCE, clientEmail))
                    .with(user(employeeEmail).roles("EMPLOYEE"))
                    .flashAttr(WebConstants.ATTR_ADD_BALANCE_DTO, dto)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, clientEmail)))
                .andExpect(flash().attributeExists(WebConstants.ATTR_SUCCESS_MESSAGE));
        }

        @Test
        void testAddBalanceToClient_ShouldRedirectToClientDetail_WhenValidationFails() throws Exception {
            String clientEmail = "test@test.com";
            String employeeEmail = "test@emp.com";
            AddBalanceDTO dto = AddBalanceDTO.builder().amount(BigDecimal.ZERO).build();
            ClientDisplayDTO clientDisplayDTO = ClientDisplayDTO.builder().email(clientEmail).balance(BigDecimal.ZERO).build();

            when(clientService.addBalanceToClient(clientEmail, dto)).thenReturn(clientDisplayDTO);

            mockMvc.perform(post(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_ADD_BALANCE, clientEmail))
                    .with(user(employeeEmail).roles("EMPLOYEE"))
                    .flashAttr(WebConstants.ATTR_ADD_BALANCE_DTO, dto)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, clientEmail)))
                .andExpect(flash().attributeExists(WebConstants.ATTR_ERROR_MESSAGE));
        }

        @Test
        void testAddBalanceToClient_ShouldRedirectToClientDetail_WhenAddingBalanceToClientFails() throws Exception {
            String clientEmail = "test@test.com";
            String employeeEmail = "test@emp.com";
            AddBalanceDTO dto = AddBalanceDTO.builder().amount(BigDecimal.ZERO).build();

            when(clientService.addBalanceToClient(clientEmail, dto)).thenThrow(new RuntimeException());

            mockMvc.perform(post(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_ADD_BALANCE, clientEmail) )
                    .with(user(employeeEmail).roles("EMPLOYEE"))
                    .flashAttr(WebConstants.ATTR_ADD_BALANCE_DTO, dto)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, clientEmail)))
                .andExpect(flash().attributeExists(WebConstants.ATTR_ERROR_MESSAGE));
        }
    }
}
