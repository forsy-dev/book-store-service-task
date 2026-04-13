package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.forsy.conf.jwt.JwtUtils;
import com.forsy.dto.CreateOrderRequestDto;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.OrderDisplayDto;
import com.forsy.service.EmployeeService;
import com.forsy.service.OrderService;
import com.forsy.util.CartCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OrderService orderService;

  @MockBean
  private EmployeeService employeeService;

  @MockBean
  CartCookieUtil cartCookieUtil;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private UserDetailsService userDetailsService;

  @Nested
  class GetOrdersForUser {

    @ParameterizedTest
    @ValueSource(strings = {"CLIENT", "EMPLOYEE"})
    void testGetOrderForUserWhenTryingToAccessOtherUserOrdersShouldReturnError() throws Exception {
      String otherEmail = "test1@test.com";
      String email = "test@test.com";

      mockMvc.perform(get("/orders/{email}", otherEmail)
                          .with(user(email).roles("CLIENT")))
          .andExpect(status().isForbidden())
          .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("Return all client orders when no keyword is provided")
    void getClientOrdersWithoutKeyword() throws Exception {
      String email = "test@test.com";
      Page<OrderDisplayDto> orders = Page.empty();

      when(orderService.getOrdersByClient(eq(email), any(Pageable.class), nullable(String.class)))
          .thenReturn(orders);

      mockMvc.perform(get("/orders/{email}", email)
                          .with(user(email).roles("CLIENT")))
          .andExpect(status().isOk())
          .andExpect(view().name("orders"));
    }

    @Test
    @DisplayName("Return selected orders for client when a search keyword is provided")
    void getClientOrdersWithKeyword() throws Exception {
      String email = "test@test.com";
      Page<OrderDisplayDto> orders = Page.empty();
      String keyword = "keyword";

      when(orderService.getOrdersByClient(eq(email), any(Pageable.class), eq(keyword)))
          .thenReturn(orders);

      mockMvc.perform(get("/orders/{email}", email)
                          .param("keyword", keyword)
                          .with(user(email).roles("CLIENT")))
          .andExpect(status().isOk())
          .andExpect(view().name("orders"));
    }

    @Test
    @DisplayName("Return all employee-managed orders when no keyword is provided")
    void getEmployeeOrdersWithoutKeyword() throws Exception {
      String email = "test@test.com";
      Page<OrderDisplayDto> orders = Page.empty();

      when(orderService.getOrdersByEmployee(eq(email), any(Pageable.class), nullable(String.class)))
          .thenReturn(orders);

      mockMvc.perform(get("/orders/{email}", email)
                          .with(user(email).roles("EMPLOYEE")))
          .andExpect(status().isOk())
          .andExpect(view().name("orders"));
    }

    @Test
    @DisplayName("Return selected orders for employee when a search keyword is provided")
    void getEmployeeOrdersWithKeyword() throws Exception {
      String email = "test@test.com";
      Page<OrderDisplayDto> orders = Page.empty();
      String keyword = "keyword";

      when(orderService.getOrdersByEmployee(eq(email), any(Pageable.class), eq(keyword)))
          .thenReturn(orders);

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
    void testGetAllOrdersWhenAuthenticatedAsClientShouldRedirect() throws Exception {
      String email = "test@test.com";

      mockMvc.perform(get("/orders")
                          .with(user(email).roles("CLIENT")))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/orders/" + email));
    }

    @Test
    void testGetAllOrdersWhenAuthenticatedAsEmployeeShouldReturnOrders() throws Exception {
      String email = "test@test.com";

      when(orderService.getAllOrders(any(Pageable.class), nullable(String.class)))
          .thenReturn(Page.empty());

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
    void testSubmitOrderWhenCartIsEmptyShouldRedirect() throws Exception {
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
    void testSubmitOrderWhenNoEmployeeFoundShouldRedirect() throws Exception {
      Map<String, Integer> cart = new HashMap<>();
      cart.put("book", 1);

      when(cartCookieUtil.getCartFromCookie(any(HttpServletRequest.class))).thenReturn(cart);
      when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(Page.empty());

      String email = "test@test.com";

      mockMvc.perform(post("/orders/submit")
                          .with(user(email).roles("CLIENT"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void testSubmitOrderWhenAddingOrderFailsShouldRedirect() throws Exception {
      String email = "test@test.com";
      Map<String, Integer> cart = new HashMap<>();
      cart.put("book", 1);

      String employeeEmail = "emp@emp.com";
      EmployeeDisplayDto employeeDisplayDto = EmployeeDisplayDto.builder()
          .email(employeeEmail).build();
      Page<EmployeeDisplayDto> page = new PageImpl<>(java.util.List.of(employeeDisplayDto));

      when(cartCookieUtil.getCartFromCookie(any(HttpServletRequest.class))).thenReturn(cart);
      when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
      when(orderService.addOrder(any(CreateOrderRequestDto.class)))
          .thenThrow(new RuntimeException("Error"));

      mockMvc.perform(post("/orders/submit")
                          .with(user(email).roles("CLIENT"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void testSubmitOrderWhenAddingOrderSucceedsShouldRedirect() throws Exception {
      String email = "test@test.com";
      Map<String, Integer> cart = new HashMap<>();
      cart.put("book", 1);

      String employeeEmail = "emp@emp.com";
      EmployeeDisplayDto employeeDisplayDto = EmployeeDisplayDto.builder()
          .email(employeeEmail).build();
      Page<EmployeeDisplayDto> page = new PageImpl<>(java.util.List.of(employeeDisplayDto));

      when(cartCookieUtil.getCartFromCookie(any(HttpServletRequest.class))).thenReturn(cart);
      when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);
      when(orderService.addOrder(any(CreateOrderRequestDto.class)))
          .thenReturn(OrderDisplayDto.builder().build());

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
    void testCancelOrderWhenSuccessShouldRedirect() throws Exception {
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
    void testCancelOrderWhenFailsShouldRedirect() throws Exception {
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
    void testConfirmOrderWhenSuccessShouldRedirect() throws Exception {
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
    void testConfirmOrderWhenFailureShouldRedirect() throws Exception {
      long orderId = 1L;
      String email = "test@test.com";

      doThrow(new RuntimeException("Error occurred")).when(orderService)
          .confirmOrder(orderId, email);

      mockMvc.perform(post("/orders/{id}/confirm", orderId)
                          .with(user(email).roles("EMPLOYEE"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/orders"))
          .andExpect(flash().attributeExists("errorMessage"));
    }
  }
}
