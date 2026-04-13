package com.forsy.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.forsy.conf.jwt.JwtUtils;
import com.forsy.dto.ChangePasswordDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.ClientUpdateDto;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.EmployeeUpdateDto;
import com.forsy.exception.InvalidPasswordException;
import com.forsy.exception.NotFoundException;
import com.forsy.service.ClientService;
import com.forsy.service.EmployeeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClientService clientService;

  @MockBean
  private EmployeeService employeeService;

  @MockBean
  private ModelMapper mapper;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private UserDetailsService userDetailsService;

  @Nested
  class GetProfilePage {

    @Test
    void testGetProfile_WhenAuthenticatedAsClient_ShouldReturnProfile() throws Exception {
      String email = "email";
      ClientDisplayDto client = ClientDisplayDto.builder().email(email).build();
      ClientUpdateDto clientUpdateDTO = ClientUpdateDto.builder().build();
      ChangePasswordDto changePasswordDTO = new ChangePasswordDto();
      EmployeeUpdateDto employeeUpdateDTO = new EmployeeUpdateDto();

      when(clientService.getClientByEmail(email)).thenReturn(client);
      when(mapper.map(client, ClientUpdateDto.class)).thenReturn(clientUpdateDTO);

      mockMvc.perform(get("/profile")
                          .with(user(email).roles("CLIENT")))
          .andExpect(status().isOk())
          .andExpect(view().name("profile"))
          .andExpect(model().attribute("changePasswordDTO", changePasswordDTO))
          .andExpect(model().attribute("userProfile", client))
          .andExpect(model().attribute("clientUpdateDTO", clientUpdateDTO))
          .andExpect(model().attribute("employeeUpdateDTO", employeeUpdateDTO));
    }

    @Test
    void testGetProfile_WhenAuthenticatedAsClient_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
      String email = "email";

      when(clientService.getClientByEmail(email)).thenThrow(NotFoundException.class);

      mockMvc.perform(get("/profile")
                          .with(user(email).roles("CLIENT")))
          .andExpect(status().isNotFound())
          .andExpect(view().name("error"));
    }

    @Test
    void testGetProfile_WhenAuthenticatedAsEmployee_ShouldReturnProfile() throws Exception {
      String email = "email";
      EmployeeDisplayDto employee = EmployeeDisplayDto.builder().email(email).build();
      EmployeeUpdateDto employeeUpdateDTO = EmployeeUpdateDto.builder().build();
      ChangePasswordDto changePasswordDTO = new ChangePasswordDto();
      ClientUpdateDto clientUpdateDTO = new ClientUpdateDto();

      when(employeeService.getEmployeeByEmail(email)).thenReturn(employee);
      when(mapper.map(employee, EmployeeUpdateDto.class)).thenReturn(employeeUpdateDTO);

      mockMvc.perform(get("/profile")
                          .with(user(email).roles("EMPLOYEE")))
          .andExpect(status().isOk())
          .andExpect(view().name("profile"))
          .andExpect(model().attribute("changePasswordDTO", changePasswordDTO))
          .andExpect(model().attribute("userProfile", employee))
          .andExpect(model().attribute("clientUpdateDTO", clientUpdateDTO))
          .andExpect(model().attribute("employeeUpdateDTO", employeeUpdateDTO));
    }

    @Test
    void testGetProfile_WhenAuthenticatedAsEmployee_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
      String email = "email";

      when(employeeService.getEmployeeByEmail(email)).thenThrow(NotFoundException.class);

      mockMvc.perform(get("/profile")
                          .with(user(email).roles("EMPLOYEE")))
          .andExpect(status().isNotFound())
          .andExpect(view().name("error"));
    }
  }

  @Nested
  class ChangePassword {

    @Test
    void testChangePassword_WhenAuthenticatedAsClient_ShouldRedirectToProfile() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "Te$t1234";
      ChangePasswordDto changePasswordDTO = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      doNothing().when(clientService).changePassword(email, changePasswordDTO);

      mockMvc.perform(put("/profile/password")
                          .with(user(email).roles("CLIENT"))
                          .with(csrf())
                          .flashAttr("changePasswordDTO", changePasswordDTO))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile"))
          .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testChangePassword_WhenAuthenticatedAsClient_ShouldRedirectToProfile_WhenValidationFails() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "";
      ChangePasswordDto changePasswordDTO = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      mockMvc.perform(put("/profile/password")
                          .with(user(email).roles("CLIENT"))
                          .with(csrf())
                          .flashAttr("changePasswordDTO", changePasswordDTO))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile?error=validation"))
          .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.changePasswordDTO"))
          .andExpect(flash().attributeExists("changePasswordDTO"));
    }

    @Test
    void testChangePassword_WhenAuthenticatedAsClient_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "Te$t1234";
      ChangePasswordDto changePasswordDTO = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      doThrow(new NotFoundException("Not Found")).when(clientService).changePassword(email, changePasswordDTO);

      mockMvc.perform(put("/profile/password")
                          .with(user(email).roles("CLIENT"))
                          .with(csrf())
                          .flashAttr("changePasswordDTO", changePasswordDTO))
          .andExpect(status().isNotFound())
          .andExpect(view().name("error"));
    }

    @Test
    void testChangePassword_WhenAuthenticatedAsClient_ShouldRedirectToProfile_WhenPasswordIncorrect() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "Te$t1234";
      ChangePasswordDto changePasswordDTO = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      doThrow(new InvalidPasswordException("Invalid password")).when(clientService).changePassword(email, changePasswordDTO);

      mockMvc.perform(put("/profile/password")
                          .with(user(email).roles("CLIENT"))
                          .with(csrf())
                          .flashAttr("changePasswordDTO", changePasswordDTO))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile?error=service"))
          .andExpect(flash().attributeExists("changePasswordDTO"))
          .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testChangePassword_WhenAuthenticatedAsEmployee_ShouldRedirectToProfile() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "Te$t1234";
      ChangePasswordDto changePasswordDTO = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      doNothing().when(employeeService).changePassword(email, changePasswordDTO);

      mockMvc.perform(put("/profile/password")
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("changePasswordDTO", changePasswordDTO))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile"))
          .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testChangePassword_WhenAuthenticatedAsEmployee_ShouldRedirectToProfile_WhenValidationFails() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "";
      ChangePasswordDto changePasswordDTO = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      mockMvc.perform(put("/profile/password")
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("changePasswordDTO", changePasswordDTO))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile?error=validation"))
          .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.changePasswordDTO"))
          .andExpect(flash().attributeExists("changePasswordDTO"));
    }

    @Test
    void testChangePassword_WhenAuthenticatedAsEmployee_ShouldReturnErrorPage_WhenEmailNotFound() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "Te$t1234";
      ChangePasswordDto changePasswordDTO = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      doThrow(new NotFoundException("Not Found")).when(employeeService).changePassword(email, changePasswordDTO);

      mockMvc.perform(put("/profile/password")
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("changePasswordDTO", changePasswordDTO))
          .andExpect(status().isNotFound())
          .andExpect(view().name("error"));
    }

    @Test
    void testChangePassword_WhenAuthenticatedAsEmployee_ShouldRedirectToProfile_WhenPasswordIncorrect() throws Exception {
      String email = "email";
      String oldPassword = "oldPassword";
      String newPassword = "Te$t1234";
      ChangePasswordDto changePasswordDTO = ChangePasswordDto.builder()
          .oldPassword(oldPassword)
          .newPassword(newPassword)
          .build();

      doThrow(new InvalidPasswordException("Invalid password")).when(employeeService).changePassword(email, changePasswordDTO);

      mockMvc.perform(put("/profile/password")
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("changePasswordDTO", changePasswordDTO))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile?error=service"))
          .andExpect(flash().attributeExists("changePasswordDTO"))
          .andExpect(flash().attributeExists("errorMessage"));
    }
  }
}
