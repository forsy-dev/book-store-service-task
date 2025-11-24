package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Nested
    class UpdateClient {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testUpdateEmployee_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {

            mockMvc.perform(put("/employees/profile"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testUpdateEmployee_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {

            String email = "test@test.com";
            String phone = "1234567890";
            LocalDate dateOfBirth = LocalDate.now().minusYears(18);
            String name = "name";
            EmployeeUpdateDTO employeeUpdateDTO = EmployeeUpdateDTO.builder()
                    .name(name)
                    .phone(phone)
                    .birthDate(dateOfBirth)
                    .build();
            EmployeeDisplayDTO employeeDisplayDTO = new EmployeeDisplayDTO();

            when(employeeService.updateEmployeeByEmail(eq(email), any(EmployeeUpdateDTO.class))).thenReturn(employeeDisplayDTO);

            mockMvc.perform(put("/employees/profile")
                            .flashAttr("employeeUpdateDTO", employeeUpdateDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"));
        }
    }
}
