package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.forsy.dto.AddBalanceDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.ClientUpdateDto;
import com.forsy.service.ClientService;
import com.forsy.util.WebConstants;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
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

@SpringBootTest
@AutoConfigureMockMvc
class ClientSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClientService clientService;

  @Nested
  class GetClients {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testGetClientsWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      mockMvc.perform(get(WebConstants.URL_CLIENTS))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetClientsWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      Page<ClientDisplayDto> clientPage = new PageImpl<>(Collections.singletonList(
          new ClientDisplayDto()));

      when(clientService.getAllClients(any(Pageable.class), nullable(String.class)))
          .thenReturn(clientPage);

      mockMvc.perform(get(WebConstants.URL_CLIENTS))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class GetClientByEmail {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testGetClientByEmailWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      String email = "email";

      mockMvc.perform(get(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, email)))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetClientByEmailWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      String email = "email";

      when(clientService.getClientByEmail(email)).thenReturn(new ClientDisplayDto());

      mockMvc.perform(get(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, email)))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class UpdateClient {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testUpdateClientWhenAuthenticatedAsClientShouldAllowAccess() throws Exception {
      String email = "test@test.com";
      String name = "name";
      ClientUpdateDto clientUpdateDto = ClientUpdateDto.builder()
          .name(name)
          .build();
      ClientDisplayDto clientDisplayDto = new ClientDisplayDto();

      when(clientService.updateClientByEmail(eq(email), any(ClientUpdateDto.class)))
          .thenReturn(clientDisplayDto);

      mockMvc.perform(put(WebConstants.URL_CLIENT_PROFILE)
                          .flashAttr(WebConstants.ATTR_CLIENT_UPDATE_DTO, clientUpdateDto))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.URL_PROFILE));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testUpdateClientWhenAuthenticatedAsEmployeeShouldForbidAccess() throws Exception {

      mockMvc.perform(put(WebConstants.URL_CLIENT_PROFILE))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class DeleteClient {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testDeleteClientWhenAuthenticatedAsClientShouldAllowAccess() throws Exception {
      String email = "test@test.com";

      doNothing().when(clientService).deleteClientByEmail(email);

      mockMvc.perform(delete(WebConstants.URL_CLIENT_PROFILE))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.addParameters(
              WebConstants.URL_LOGIN, Map.of(WebConstants.PARAM_ACCOUNT_DELETED, "true"))));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testDeleteClientWhenAuthenticatedAsEmployeeShouldForbidAccess() throws Exception {

      mockMvc.perform(delete(WebConstants.URL_CLIENT_PROFILE))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class BlockClient {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testBlockClientWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      String email = "test@test.com";

      mockMvc.perform(put(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_BLOCK, email)))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testBlockClientWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      String email = "test@test.com";

      doNothing().when(clientService).blockClient(email);

      mockMvc.perform(put(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_BLOCK, email)))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.expandPathVariables(
              WebConstants.URL_CLIENT_DETAIL, email)));
    }
  }

  @Nested
  class UnblockClient {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testUnblockClientWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      String email = "test@test.com";

      mockMvc.perform(put(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_UNBLOCK, email)))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testUnblockClientWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      String email = "test@test.com";

      doNothing().when(clientService).unblockClient(email);

      mockMvc.perform(put(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_UNBLOCK, email)))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.expandPathVariables(
              WebConstants.URL_CLIENT_DETAIL, email)));
    }
  }

  @Nested
  class AddBalanceToClient {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testAddBalanceToClientWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      String email = "test@test.com";

      mockMvc.perform(put(WebConstants.expandPathVariables(
          WebConstants.URL_CLIENT_ADD_BALANCE, email)))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testAddBalanceToClientWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      String clientEmail = "test@test.com";
      AddBalanceDto dto = AddBalanceDto.builder().amount(BigDecimal.TEN).build();
      ClientDisplayDto clientDisplayDto = ClientDisplayDto.builder().email(clientEmail)
          .balance(BigDecimal.TEN).build();

      when(clientService.addBalanceToClient(clientEmail, dto)).thenReturn(clientDisplayDto);

      mockMvc.perform(post(WebConstants.expandPathVariables(
          WebConstants.URL_CLIENT_ADD_BALANCE, clientEmail))
                          .flashAttr(WebConstants.ATTR_ADD_BALANCE_DTO, dto))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.expandPathVariables(
              WebConstants.URL_CLIENT_DETAIL, clientEmail)))
          .andExpect(flash().attributeExists(WebConstants.ATTR_SUCCESS_MESSAGE));
    }
  }
}
