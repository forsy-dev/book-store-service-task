package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.jwt.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.EmployeeDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

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
        void testUpdateEmployee_ShouldRedirectToProfile_WhenSuccess() throws Exception {
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
                            .flashAttr("employeeUpdateDTO", employeeUpdateDTO)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"))
                    .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        void testUpdateEmployee_ShouldRedirectToProfile_WhenValidationFails() throws Exception {
            String email = "a";
            String phone = "1";
            LocalDate dateOfBirth = LocalDate.now().minusYears(18);
            String name = "name";
            EmployeeUpdateDTO employeeUpdateDTO = EmployeeUpdateDTO.builder()
                    .name(name)
                    .phone(phone)
                    .birthDate(dateOfBirth)
                    .build();


            mockMvc.perform(put("/employees/profile")
                            .flashAttr("employeeUpdateDTO", employeeUpdateDTO)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile?error=validation"))
                    .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.employeeUpdateDTO"))
                    .andExpect(flash().attributeExists("employeeUpdateDTO"));
        }
    }
}
