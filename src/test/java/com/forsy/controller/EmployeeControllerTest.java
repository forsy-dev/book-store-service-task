package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.forsy.conf.jwt.JwtUtils;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.EmployeeUpdateDto;
import com.forsy.service.EmployeeService;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EmployeeService employeeService;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private UserDetailsService userDetailsService;

  @Nested
  class UpdateClient {

    @Test
    void testUpdateEmployeeShouldRedirectToProfileWhenSuccess() throws Exception {
      String email = "test@test.com";
      String phone = "1234567890";
      LocalDate dateOfBirth = LocalDate.now().minusYears(18);
      String name = "name";
      EmployeeUpdateDto employeeUpdateDto = EmployeeUpdateDto.builder()
          .name(name)
          .phone(phone)
          .birthDate(dateOfBirth)
          .build();
      EmployeeDisplayDto employeeDisplayDto = new EmployeeDisplayDto();

      when(employeeService.updateEmployeeByEmail(eq(email), any(EmployeeUpdateDto.class)))
          .thenReturn(employeeDisplayDto);

      mockMvc.perform(put("/employees/profile")
                          .flashAttr("employeeUpdateDTO", employeeUpdateDto)
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile"))
          .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testUpdateEmployeeShouldRedirectToProfileWhenValidationFails() throws Exception {
      String email = "a";
      String phone = "1";
      LocalDate dateOfBirth = LocalDate.now().minusYears(18);
      String name = "name";
      EmployeeUpdateDto employeeUpdateDto = EmployeeUpdateDto.builder()
          .name(name)
          .phone(phone)
          .birthDate(dateOfBirth)
          .build();


      mockMvc.perform(put("/employees/profile")
                          .flashAttr("employeeUpdateDTO", employeeUpdateDto)
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile?error=validation"))
          .andExpect(flash().attributeExists(
              "org.springframework.validation.BindingResult.employeeUpdateDTO"))
          .andExpect(flash().attributeExists("employeeUpdateDTO"));
    }
  }
}
