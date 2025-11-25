package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ClientService clientService;

    @Nested
    class GetOrdersForUser {

        @Test
        void testGetOrderForUser_WhenAuthenticatedAsClientAndTryingToAccessOtherUserOrders_ShouldReturnError() throws Exception {
            String otherEmail = "test1@test.com";
            String email = "test@test.com";

            mockMvc.perform(get("/orders/{email}", otherEmail)
                            .with(user(email).roles("CLIENT")))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error"));
        }

        @Test
        void testGetOrderForUser_WhenAuthenticatedAsClient_ShouldReturnOrders() throws Exception {
            String email = "test@test.com";
            ClientDisplayDTO clientDisplayDTO = ClientDisplayDTO.builder().email(email).build();
            Page<OrderDisplayDTO> orders = Page.empty();

            when(clientService.getClientByEmail(email)).thenReturn(clientDisplayDTO);
            when(orderService.getOrdersByClient(eq(email), any(Pageable.class))).thenReturn(orders);

            mockMvc.perform(get("/orders/{email}", email)
                            .with(user(email).roles("CLIENT")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders"));
        }

        @Test
        void testGetOrderForUser_WhenAuthenticatedAsEmployee_ShouldReturnOrders() throws Exception {
            String email = "test@test.com";
            EmployeeDisplayDTO employeeDisplayDTO = EmployeeDisplayDTO.builder().email(email).build();
            Page<OrderDisplayDTO> orders = Page.empty();

            when(clientService.getClientByEmail(email)).thenThrow(NotFoundException.class);
            when(employeeService.getEmployeeByEmail(email)).thenReturn(employeeDisplayDTO);
            when(orderService.getOrdersByEmployee(eq(email), any(Pageable.class))).thenReturn(orders);

            mockMvc.perform(get("/orders/{email}", email)
                            .with(user(email).roles("EMPLOYEE")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders"));
        }

        @Test
        void testGetOrderForUser_WhenUserNotFound_ShouldReturnError() throws Exception {
            String email = "test@test.com";

            when(clientService.getClientByEmail(email)).thenThrow(NotFoundException.class);
            when(employeeService.getEmployeeByEmail(email)).thenThrow(NotFoundException.class);

            mockMvc.perform(get("/orders/{email}", email)
                            .with(user(email).roles("EMPLOYEE")))
                    .andExpect(status().isNotFound())
                    .andExpect(view().name("error"));
        }
    }

    @Nested
    class GetAllOrders {

        @Test
        void testGetAllOrders_WhenAuthenticatedAsClient_ShouldRedirect() throws Exception {
            String email = "test@test.com";

            mockMvc.perform(get("/orders")
                            .with(user(email).roles("CLIENT")))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders/" + email));
        }

        @Test
        void testGetAllOrders_WhenAuthenticatedAsEmployee_ShouldReturnOrders() throws Exception {
            String email = "test@test.com";

            when(orderService.getAllOrders(any(Pageable.class))).thenReturn(Page.empty());

            mockMvc.perform(get("/orders")
                            .with(user(email).roles("EMPLOYEE")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders"))
                    .andExpect(model().attributeExists("orderPage"));
        }
    }

    @Nested
    class SubmitOrder {

        @Test
        void testSubmitOrder_WhenCartIsEmpty_ShouldRedirect() throws Exception {
            String email = "test@test.com";
            Map<String, Integer> cart = new HashMap<>();

            mockMvc.perform(post("/orders/submit")
                            .sessionAttr("CART", cart)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cart"));
        }

        @Test
        void testSubmitOrder_WhenNoEmployeeFound_ShouldRedirect() throws Exception {
            String email = "test@test.com";
            Map<String, Integer> cart = new HashMap<>();
            cart.put("book", 1);

            when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(Page.empty());

            mockMvc.perform(post("/orders/submit")
                            .sessionAttr("CART", cart)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cart"));
        }

        @Test
        void testSubmitOrder_WhenAddingOrderFails_ShouldRedirect() throws Exception {
            String email = "test@test.com";
            Map<String, Integer> cart = new HashMap<>();
            cart.put("book", 1);

            String employeeEmail = "emp@emp.com";
            EmployeeDisplayDTO employeeDisplayDTO = EmployeeDisplayDTO.builder().email(employeeEmail).build();
            Page<EmployeeDisplayDTO> page = new PageImpl<>(java.util.List.of(employeeDisplayDTO));

            when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
            when(orderService.addOrder(any(CreateOrderRequestDTO.class))).thenThrow(new RuntimeException("Error"));

            mockMvc.perform(post("/orders/submit")
                            .sessionAttr("CART", cart)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cart"));
        }

        @Test
        void testSubmitOrder_WhenAddingOrderSucceeds_ShouldRedirect() throws Exception {
            String email = "test@test.com";
            Map<String, Integer> cart = new HashMap<>();
            cart.put("book", 1);

            String employeeEmail = "emp@emp.com";
            EmployeeDisplayDTO employeeDisplayDTO = EmployeeDisplayDTO.builder().email(employeeEmail).build();
            Page<EmployeeDisplayDTO> page = new PageImpl<>(java.util.List.of(employeeDisplayDTO));

            when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
            when(orderService.addOrder(any(CreateOrderRequestDTO.class))).thenReturn(OrderDisplayDTO.builder().build());

            mockMvc.perform(post("/orders/submit")
                            .sessionAttr("CART", cart)
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }
    }
}
