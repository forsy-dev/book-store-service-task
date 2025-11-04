package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

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

    @Nested
    class GetProfilePage {

        @Test
        void testGetProfile_WhenAuthenticatedAsClient_ShouldReturnProfile() throws Exception {
            String email = "email";
            ClientDisplayDTO client = ClientDisplayDTO.builder().email(email).build();
            ClientUpdateDTO clientUpdateDTO = ClientUpdateDTO.builder().build();
            ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
            EmployeeUpdateDTO employeeUpdateDTO = new EmployeeUpdateDTO();

            when(clientService.getClientByEmail(email)).thenReturn(client);
            when(mapper.map(client, ClientUpdateDTO.class)).thenReturn(clientUpdateDTO);

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
            EmployeeDisplayDTO employee = EmployeeDisplayDTO.builder().email(email).build();
            EmployeeUpdateDTO employeeUpdateDTO = EmployeeUpdateDTO.builder().build();
            ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
            ClientUpdateDTO clientUpdateDTO = new ClientUpdateDTO();

            when(employeeService.getEmployeeByEmail(email)).thenReturn(employee);
            when(mapper.map(employee, EmployeeUpdateDTO.class)).thenReturn(employeeUpdateDTO);

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
}
