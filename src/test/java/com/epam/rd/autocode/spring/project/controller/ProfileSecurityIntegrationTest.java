package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class ProfileSecurityIntegrationTest {

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
        void testGetProfile_WhenAuthenticatedAsClient_ShouldReturnProfile() throws Exception {
            String email = "email";
            ClientDisplayDTO client = ClientDisplayDTO.builder().email(email).build();
            ClientUpdateDTO clientUpdateDTO = ClientUpdateDTO.builder().build();

            when(clientService.getClientByEmail(email)).thenReturn(client);
            when(mapper.map(client, ClientUpdateDTO.class)).thenReturn(clientUpdateDTO);

            mockMvc.perform(get("/profile"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE", username = "email")
        void testGetProfile_WhenAuthenticatedAsEmployee_ShouldReturnProfile() throws Exception {
            String email = "email";
            EmployeeDisplayDTO employee = EmployeeDisplayDTO.builder().email(email).build();
            EmployeeUpdateDTO employeeUpdateDTO = EmployeeUpdateDTO.builder().build();

            when(employeeService.getEmployeeByEmail(email)).thenReturn(employee);
            when(mapper.map(employee, EmployeeUpdateDTO.class)).thenReturn(employeeUpdateDTO);

            mockMvc.perform(get("/profile"))
                    .andExpect(status().isOk());
        }
    }
}
