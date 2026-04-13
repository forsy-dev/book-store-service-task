package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.EmployeeUpdateDto;
import com.forsy.service.EmployeeService;
import java.time.LocalDate;
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
class EmployeeSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private EmployeeService employeeService;

  @Nested
  class UpdateClient {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testUpdateEmployeeWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {

      mockMvc.perform(put("/employees/profile"))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testUpdateEmployeeWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {

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
                          .flashAttr("employeeUpdateDTO", employeeUpdateDto))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/profile"));
    }
  }
}
