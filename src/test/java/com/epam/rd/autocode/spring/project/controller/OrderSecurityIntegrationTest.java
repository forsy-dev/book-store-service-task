package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ClientService clientService;

    @Nested
    class GetAllOrders {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetAllOrders_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {
            String email = "test@test.com";

            mockMvc.perform(get("/orders"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders/" + email));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetAllOrders_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {

            when(orderService.getAllOrders(any(Pageable.class))).thenReturn(Page.empty());

            mockMvc.perform(get("/orders"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders"));
        }
    }

    @Nested
    class GetOrdersForUser {

        @Test
        @WithMockUser(roles = "CLIENT", username = "test@test.com")
        void testGetOrderForUser_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {

            String email = "test@test.com";
            ClientDisplayDTO clientDisplayDTO = ClientDisplayDTO.builder().email(email).build();
            Page<OrderDisplayDTO> orders = Page.empty();

            when(clientService.getClientByEmail(email)).thenReturn(clientDisplayDTO);
            when(orderService.getOrdersByClient(eq(email), any(Pageable.class))).thenReturn(orders);

            mockMvc.perform(get("/orders/{email}", email))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENT", username = "test@test.com")
        void testGetOrderForUser_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {

            String email = "test@test.com";
            EmployeeDisplayDTO employeeDisplayDTO = EmployeeDisplayDTO.builder().email(email).build();
            Page<OrderDisplayDTO> orders = Page.empty();

            when(clientService.getClientByEmail(email)).thenThrow(NotFoundException.class);
            when(employeeService.getEmployeeByEmail(email)).thenReturn(employeeDisplayDTO);
            when(orderService.getOrdersByEmployee(eq(email), any(Pageable.class))).thenReturn(orders);

            mockMvc.perform(get("/orders/{email}", email))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class SubmitOrder {

        @Test
        @WithMockUser(roles = "CLIENT", username = "test@test.com")
        void testSubmitOrder_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {

            Map<String, Integer> cart = new HashMap<>();
            cart.put("book", 1);

            String employeeEmail = "emp@emp.com";
            EmployeeDisplayDTO employeeDisplayDTO = EmployeeDisplayDTO.builder().email(employeeEmail).build();
            Page<EmployeeDisplayDTO> page = new PageImpl<>(java.util.List.of(employeeDisplayDTO));

            when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
            when(orderService.addOrder(any(CreateOrderRequestDTO.class))).thenReturn(OrderDisplayDTO.builder().build());

            mockMvc.perform(post("/orders/submit")
                            .sessionAttr("CART", cart))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE", username = "test@test.com")
        void testSubmitOrder_WhenAuthenticatedAsEmployee_ShouldForbidAccess() throws Exception {

            mockMvc.perform(post("/orders/submit"))
                    .andExpect(status().isForbidden());
        }
    }
}
