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

    ClientCreateDto clientCreateDTO;
    ClientDisplayDto clientDisplayDTO;

    @BeforeEach
    void setUp() {
      clientCreateDTO = ClientCreateDto.builder()
          .name("testclient")
          .email("test@test.com")
          .password("Te$t1234")
          .build();
      clientDisplayDTO = ClientDisplayDto.builder()
          .name("testclient")
          .email("test@test.com")
          .build();
    }

    @Test
    void testRegisterClient_WhenAnonymous_ShouldRedirectToLoginPage() throws Exception {

      when(clientService.addClient(any(ClientCreateDto.class))).thenReturn(clientDisplayDTO);

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
