package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.forsy.dto.ClientCreateDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HomeSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClientService clientService;

  @Test
  void testGetLoginPageWhenAnonymousShouldReturnLoginPage() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk());
  }

  @Nested
  class GetRegisterPage {

    @Test
    void testGetRegisterPageWhenAnonymousShouldReturnRegisterPage() throws Exception {
      mockMvc.perform(get("/register"))
          .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testGetRegisterPageWhenAuthenticatedAsClientShouldRedirect() throws Exception {
      mockMvc.perform(get("/register"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetRegisterPageWhenAuthenticatedAsEmployeeShouldRedirect() throws Exception {
      mockMvc.perform(get("/register"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }
  }

  @Nested
  class RegisterClient {

    ClientCreateDto clientCreateDto;
    ClientDisplayDto clientDisplayDto;

    @BeforeEach
    void setUp() {
      clientCreateDto = ClientCreateDto.builder()
          .name("testclient")
          .email("test@test.com")
          .password("Te$t1234")
          .build();
      clientDisplayDto = ClientDisplayDto.builder()
          .name("testclient")
          .email("test@test.com")
          .build();
    }

    @Test
    void testRegisterClientWhenAnonymousShouldRedirectToLoginPage() throws Exception {

      when(clientService.addClient(any(ClientCreateDto.class))).thenReturn(clientDisplayDto);

      mockMvc.perform(post("/register")
                          .flashAttr("client", clientCreateDto))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void testRegisterClientWhenAuthorizedAsClientShouldRedirectToBooksPage() throws Exception {

      mockMvc.perform(post("/register")
                          .flashAttr("client", clientCreateDto))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testRegisterClientWhenAuthorizedAsEmployeeShouldRedirectToBooksPage() throws Exception {

      mockMvc.perform(post("/register")
                          .flashAttr("client", clientCreateDto))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }
  }
}
