package com.forsy.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.forsy.dto.ChangePasswordDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.ClientUpdateDto;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.EmployeeUpdateDto;
import com.forsy.service.ClientService;
import com.forsy.service.EmployeeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClientService clientService;

  @MockBean
  private EmployeeService employeeService;

  @MockBean
  private ModelMapper mapper;

  @Nested
  class GetProfilePage {

    @Test
    @WithMockUser(roles = "CLIENT", username = "email")
    void testGetProfileWhenAuthenticatedAsClientShouldReturnProfile() throws Exception {
      String email = "email";
      ClientDisplayDto client = ClientDisplayDto.builder().email(email).build();
      ClientUpdateDto clientUpdateDto = ClientUpdateDto.builder().build();

      when(clientService.getClientByEmail(email)).thenReturn(client);
      when(mapper.map(client, ClientUpdateDto.class)).thenReturn(clientUpdateDto);

      mockMvc.perform(get("/profile"))
          .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE", username = "email")
    void testGetProfileWhenAuthenticatedAsEmployeeShouldReturnProfile() throws Exception {
      String email = "email";
      EmployeeDisplayDto employee = EmployeeDisplayDto.builder().email(email).build();
      EmployeeUpdateDto employeeUpdateDto = EmployeeUpdateDto.builder().build();

      when(employeeService.getEmployeeByEmail(email)).thenReturn(employee);
      when(mapper.map(employee, EmployeeUpdateDto.class)).thenReturn(employeeUpdateDto);

      mockMvc.perform(get("/profile"))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class ChangePassword {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testChangePasswordWhenAuthenticatedAsClientShouldRedirectToProfile() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "Te$t1234";
      ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      doNothing().when(clientService).changePassword(email, changePasswordDto);

      mockMvc.perform(put("/profile/password")
                          .flashAttr("changePasswordDTO", changePasswordDto)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testChangePasswordWhenAuthenticatedAsEmployeeShouldRedirectToProfile() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "Te$t1234";
      ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      doNothing().when(employeeService).changePassword(email, changePasswordDto);

      mockMvc.perform(put("/profile/password")
                          .flashAttr("changePasswordDTO", changePasswordDto)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile"));
    }
  }
}
