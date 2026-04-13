package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.forsy.conf.jwt.JwtUtils;
import com.forsy.dto.ClientCreateDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.exception.AlreadyExistException;
import com.forsy.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = HomeController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class HomeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClientService clientService;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private UserDetailsService userDetailsService;

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
    void testRegisterClient_ShouldRedirect() throws Exception {

      when(clientService.addClient(any(ClientCreateDto.class))).thenReturn(clientDisplayDTO);

      mockMvc.perform(post("/register")
                          .flashAttr("client", clientCreateDTO))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testRegisterClient_ShouldReturnToRegisterForm_WhenValidationFails() throws Exception {
      clientCreateDTO.setPassword("");

      when(clientService.addClient(any(ClientCreateDto.class))).thenReturn(clientDisplayDTO);

      mockMvc.perform(post("/register")
                          .flashAttr("client", clientCreateDTO))
          .andExpect(status().isOk())
          .andExpect(view().name("register-form"));
    }

    @Test
    void testRegisterClient_ShouldReturnToRegisterForm_WhenEmailAlreadyExist() throws Exception {
      when(clientService.addClient(any(ClientCreateDto.class))).thenThrow(new AlreadyExistException("Client already exists"));

      mockMvc.perform(post("/register")
                          .flashAttr("client", clientCreateDTO))
          .andExpect(status().isOk())
          .andExpect(view().name("register-form"))
          .andExpect(model().attributeExists("errorMessage"));
    }
  }
}
