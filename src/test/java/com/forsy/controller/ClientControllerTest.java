package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.forsy.conf.jwt.JwtUtils;
import com.forsy.dto.AddBalanceDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.ClientUpdateDto;
import com.forsy.exception.NotFoundException;
import com.forsy.service.ClientService;
import com.forsy.util.CartCookieUtil;
import com.forsy.util.WebConstants;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
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

@WebMvcTest(ClientController.class)
class ClientControllerTest {

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
    void testGetAllClientsShouldReturnClientsList() throws Exception {
      Page<ClientDisplayDto> clientPage =
          new PageImpl<>(Collections.singletonList(new ClientDisplayDto()));
      when(clientService.getAllClients(any(Pageable.class),
                                       nullable(String.class))).thenReturn(clientPage);

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
    void testGetClientShouldReturnClientWhenSuccess() throws Exception {
      String email = "a";
      ClientDisplayDto clientDto = ClientDisplayDto.builder().email(email).build();

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
    void testUpdateClientShouldRedirectToProfileWhenSuccess() throws Exception {
      String email = "test@test.com";
      String name = "name";
      ClientUpdateDto clientUpdateDto = ClientUpdateDto.builder()
          .name(name)
          .build();
      ClientDisplayDto clientDisplayDto = new ClientDisplayDto();

      when(clientService.updateClientByEmail(eq(email), any(ClientUpdateDto.class)))
          .thenReturn(clientDisplayDto);

      mockMvc.perform(put(WebConstants.URL_CLIENT_PROFILE)
                          .flashAttr(WebConstants.ATTR_CLIENT_UPDATE_DTO, clientUpdateDto)
                          .with(user(email).roles("CLIENT"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.URL_PROFILE))
          .andExpect(flash().attributeExists(WebConstants.ATTR_SUCCESS_MESSAGE));
    }

    @Test
    void testUpdateClientShouldRedirectToProfileWhenValidationFails() throws Exception {
      String email = "test@test.com";
      ClientUpdateDto clientUpdateDto = ClientUpdateDto.builder()
          .build();

      mockMvc.perform(put(WebConstants.URL_CLIENT_PROFILE)
                          .flashAttr(WebConstants.ATTR_CLIENT_UPDATE_DTO, clientUpdateDto)
                          .with(user(email).roles("CLIENT"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.addParameters(WebConstants.URL_PROFILE,
                                                              Map.of("error", "validation"))))
          .andExpect(flash().attributeExists(WebConstants.getBindingResultKey(
              WebConstants.ATTR_CLIENT_UPDATE_DTO)))
          .andExpect(flash().attributeExists(WebConstants.ATTR_CLIENT_UPDATE_DTO));
    }
  }

  @Nested
  class DeleteClient {

    @Test
    void testDeleteClientShouldRedirectToLoginWhenSuccess() throws Exception {
      String email = "test@test.com";

      doNothing().when(clientService).deleteClientByEmail(email);

      mockMvc.perform(delete(WebConstants.URL_CLIENT_PROFILE)
                          .with(user(email).roles("CLIENT"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.addParameters(WebConstants.URL_LOGIN,
                                                              Map.of("accountDeleted", "true"))));
    }

    @Test
    void testDeleteClientShouldReturnErrorPageWhenEmailNotFound() throws Exception {
      String email = "test@test.com";

      doThrow(new NotFoundException("Client not found")).when(clientService)
          .deleteClientByEmail(email);

      mockMvc.perform(delete(WebConstants.URL_CLIENT_PROFILE)
                          .with(user(email).roles("CLIENT"))
                          .with(csrf()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  class BlockClient {

    @Test
    void testBlockClientShouldRedirectToClientDetailWhenSuccess() throws Exception {
      String email = "test@test.com";

      doNothing().when(clientService).blockClient(email);

      mockMvc.perform(put(WebConstants.URL_CLIENT_BLOCK, email)
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL,
                                                                    email)));
    }

    @Test
    void testBlockClientShouldReturnErrorPageWhenEmailNotFound() throws Exception {
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
    void testUnblockClientShouldRedirectToClientDetailWhenSuccess() throws Exception {
      String email = "test@test.com";

      doNothing().when(clientService).unblockClient(email);

      mockMvc.perform(put(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_UNBLOCK, email))
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.expandPathVariables(
              WebConstants.URL_CLIENT_DETAIL, email)));
    }

    @Test
    void testUnblockClientShouldReturnErrorPageWhenEmailNotFound() throws Exception {
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
    void testAddBalanceToClientShouldRedirectToClientDetailWhenSuccess() throws Exception {
      String clientEmail = "test@test.com";
      String employeeEmail = "test@emp.com";
      AddBalanceDto dto = AddBalanceDto.builder().amount(BigDecimal.TEN).build();
      ClientDisplayDto clientDisplayDto = ClientDisplayDto.builder().email(clientEmail)
          .balance(BigDecimal.TEN).build();

      when(clientService.addBalanceToClient(clientEmail, dto)).thenReturn(clientDisplayDto);

      mockMvc.perform(post(WebConstants.expandPathVariables(
          WebConstants.URL_CLIENT_ADD_BALANCE, clientEmail))
                          .with(user(employeeEmail).roles("EMPLOYEE"))
                          .flashAttr(WebConstants.ATTR_ADD_BALANCE_DTO, dto)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.expandPathVariables(
              WebConstants.URL_CLIENT_DETAIL, clientEmail)))
          .andExpect(flash().attributeExists(WebConstants.ATTR_SUCCESS_MESSAGE));
    }

    @Test
    void testAddBalanceToClientShouldRedirectToClientDetailWhenValidationFails() throws Exception {
      String clientEmail = "test@test.com";
      String employeeEmail = "test@emp.com";
      AddBalanceDto dto = AddBalanceDto.builder().amount(BigDecimal.ZERO).build();
      ClientDisplayDto clientDisplayDto = ClientDisplayDto.builder().email(clientEmail)
          .balance(BigDecimal.ZERO).build();

      when(clientService.addBalanceToClient(clientEmail, dto)).thenReturn(clientDisplayDto);

      mockMvc.perform(post(WebConstants.expandPathVariables(
          WebConstants.URL_CLIENT_ADD_BALANCE, clientEmail))
                          .with(user(employeeEmail).roles("EMPLOYEE"))
                          .flashAttr(WebConstants.ATTR_ADD_BALANCE_DTO, dto)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.expandPathVariables(
              WebConstants.URL_CLIENT_DETAIL, clientEmail)))
          .andExpect(flash().attributeExists(WebConstants.ATTR_ERROR_MESSAGE));
    }

    @Test
    void addBalanceRedirectsOnFailure() throws Exception {
      String clientEmail = "test@test.com";
      String employeeEmail = "test@emp.com";
      AddBalanceDto dto = AddBalanceDto.builder().amount(BigDecimal.ZERO).build();

      when(clientService.addBalanceToClient(clientEmail, dto)).thenThrow(new RuntimeException());

      mockMvc.perform(post(WebConstants.expandPathVariables(
          WebConstants.URL_CLIENT_ADD_BALANCE, clientEmail))
                          .with(user(employeeEmail).roles("EMPLOYEE"))
                          .flashAttr(WebConstants.ATTR_ADD_BALANCE_DTO, dto)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.expandPathVariables(
              WebConstants.URL_CLIENT_DETAIL, clientEmail)))
          .andExpect(flash().attributeExists(WebConstants.ATTR_ERROR_MESSAGE));
    }
  }
}
