package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.jwt.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.util.CartCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    CartCookieUtil cartCookieUtil;

    @Nested
    class GetOrdersForUser {

        @ParameterizedTest
        @ValueSource(strings = {"CLIENT", "EMPLOYEE"})
        void testGetOrderForUser_WhenTryingToAccessOtherUserOrders_ShouldReturnError() throws Exception {
            String otherEmail = "test1@test.com";
            String email = "test@test.com";

            mockMvc.perform(get("/orders/{email}", otherEmail)
                            .with(user(email).roles("CLIENT")))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error"));
        }

        @Test
        void testGetOrderForUser_WhenAuthenticatedAsClientAndKeywordNotGiven_ShouldReturnAllOrders() throws Exception {
            String email = "test@test.com";
            Page<OrderDisplayDTO> orders = Page.empty();

            when(orderService.getOrdersByClient(eq(email), any(Pageable.class), nullable(String.class))).thenReturn(orders);

            mockMvc.perform(get("/orders/{email}", email)
                            .with(user(email).roles("CLIENT")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders"));
        }

        @Test
        void testGetOrderForUser_WhenAuthenticatedAsClientAndKeywordGiven_ShouldReturnSelectedOrders() throws Exception {
            String email = "test@test.com";
            Page<OrderDisplayDTO> orders = Page.empty();
            String keyword = "keyword";

            when(orderService.getOrdersByClient(eq(email), any(Pageable.class), eq(keyword))).thenReturn(orders);

            mockMvc.perform(get("/orders/{email}", email)
                    .param("keyword", keyword)
                    .with(user(email).roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
        }

        @Test
        void testGetOrderForUser_WhenAuthenticatedAsEmployeeAndKeywordNotGiven_ShouldReturnAllOrders() throws Exception {
            String email = "test@test.com";
            Page<OrderDisplayDTO> orders = Page.empty();

            when(orderService.getOrdersByEmployee(eq(email), any(Pageable.class), nullable(String.class))).thenReturn(orders);

            mockMvc.perform(get("/orders/{email}", email)
                    .with(user(email).roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
        }

        @Test
        void testGetOrderForUser_WhenAuthenticatedAsEmployeeAndKeywordGiven_ShouldReturnSelectedOrders() throws Exception {
            String email = "test@test.com";
            Page<OrderDisplayDTO> orders = Page.empty();
            String keyword = "keyword";

            when(orderService.getOrdersByEmployee(eq(email), any(Pageable.class), eq(keyword))).thenReturn(orders);

            mockMvc.perform(get("/orders/{email}", email)
                    .param("keyword", keyword)
                    .with(user(email).roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
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

            when(orderService.getAllOrders(any(Pageable.class), nullable(String.class))).thenReturn(Page.empty());

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

            when(cartCookieUtil.getCartFromCookie(any(HttpServletRequest.class))).thenReturn(cart);

            mockMvc.perform(post("/orders/submit")
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

            when(cartCookieUtil.getCartFromCookie(any(HttpServletRequest.class))).thenReturn(cart);
            when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(Page.empty());

            mockMvc.perform(post("/orders/submit")
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

            when(cartCookieUtil.getCartFromCookie(any(HttpServletRequest.class))).thenReturn(cart);
            when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
            when(orderService.addOrder(any(CreateOrderRequestDTO.class))).thenThrow(new RuntimeException("Error"));

            mockMvc.perform(post("/orders/submit")
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

            when(cartCookieUtil.getCartFromCookie(any(HttpServletRequest.class))).thenReturn(cart);
            when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
            when(orderService.addOrder(any(CreateOrderRequestDTO.class))).thenReturn(OrderDisplayDTO.builder().build());

            mockMvc.perform(post("/orders/submit")
                            .with(user(email).roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }
    }

    @Nested
    class CancelOrder {

        @Test
        void testCancelOrder_WhenSuccess_ShouldRedirect() throws Exception {
            long orderId = 1L;
            String email = "test@test.com";

            doNothing().when(orderService).cancelOrder(orderId, email);

            mockMvc.perform(post("/orders/{id}/cancel", orderId)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders"))
                    .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        void testCancelOrder_WhenFails_ShouldRedirect() throws Exception {
            long orderId = 1L;
            String email = "test@test.com";

            doThrow(new RuntimeException("Error")).when(orderService).cancelOrder(orderId, email);

            mockMvc.perform(post("/orders/{id}/cancel", orderId)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders"))
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    @Nested
    class ConfirmOrder {

        @Test
        void testConfirmOrder_WhenSuccess_ShouldRedirect() throws Exception {
            long orderId = 1L;
            String email = "test@test.com";

            doNothing().when(orderService).confirmOrder(orderId, email);

            mockMvc.perform(post("/orders/{id}/confirm", orderId)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders"))
                    .andExpect(flash().attributeExists("successMessage"));
        }

        @Test
        void testConfirmOrder_WhenFailure_ShouldRedirect() throws Exception {
            long orderId = 1L;
            String email = "test@test.com";

            doThrow(new RuntimeException("Error occurred")).when(orderService).confirmOrder(orderId, email);

            mockMvc.perform(post("/orders/{id}/confirm", orderId)
                            .with(user(email).roles("EMPLOYEE"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders"))
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }
}
