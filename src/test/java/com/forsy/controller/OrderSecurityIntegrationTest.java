package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.CreateOrderRequestDto;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.OrderDisplayDto;
import com.forsy.service.ClientService;
import com.forsy.service.EmployeeService;
import com.forsy.service.OrderService;
import com.forsy.util.CartCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
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

@SpringBootTest
@AutoConfigureMockMvc
class OrderSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OrderService orderService;

  @MockBean
  private EmployeeService employeeService;

  @MockBean
  private ClientService clientService;

  @MockBean
  private CartCookieUtil cartCookieUtil;

  @Nested
  class GetAllOrders {

    @Test
    @WithMockUser(roles = "CLIENT", username = "test@test.com")
    void testGetAllOrdersWhenAuthenticatedAsClientShouldAllowAccess() throws Exception {
      String email = "test@test.com";

      mockMvc.perform(get("/orders"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/orders/" + email));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetAllOrdersWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {

      when(orderService.getAllOrders(any(Pageable.class), nullable((String.class))))
          .thenReturn(Page.empty());

      mockMvc.perform(get("/orders"))
          .andExpect(status().isOk())
          .andExpect(view().name("orders"));
    }
  }

  @Nested
  class GetOrdersForUser {

    @Test
    @WithMockUser(roles = "CLIENT", username = "test@test.com")
    void testGetOrderForUserWhenAuthenticatedAsClientShouldAllowAccess() throws Exception {

      String email = "test@test.com";
      ClientDisplayDto clientDisplayDto = ClientDisplayDto.builder().email(email).build();
      Page<OrderDisplayDto> orders = Page.empty();

      when(clientService.getClientByEmail(email)).thenReturn(clientDisplayDto);
      when(orderService.getOrdersByClient(eq(email), any(Pageable.class), nullable(String.class)))
          .thenReturn(orders);

      mockMvc.perform(get("/orders/{email}", email))
          .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE", username = "test@test.com")
    void testGetOrderForUserWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {

      String email = "test@test.com";
      Page<OrderDisplayDto> orders = Page.empty();

      when(orderService.getOrdersByEmployee(eq(email), any(Pageable.class), nullable(String.class)))
          .thenReturn(orders);

      mockMvc.perform(get("/orders/{email}", email))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class SubmitOrder {

    @Test
    @WithMockUser(roles = "CLIENT", username = "test@test.com")
    void testSubmitOrderWhenAuthenticatedAsClientShouldAllowAccess() throws Exception {

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

      mockMvc.perform(post("/orders/submit").with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE", username = "test@test.com")
    void testSubmitOrderWhenAuthenticatedAsEmployeeShouldForbidAccess() throws Exception {

      mockMvc.perform(post("/orders/submit").with(csrf()))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class CancelOrder {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testCancelOrderWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {

      long orderId = 1L;

      mockMvc.perform(post("/orders/{id}/cancel", orderId).with(csrf()))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE", username = "test@test.com")
    void testCancelOrderWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {

      long orderId = 1L;
      String email = "test@test.com";

      doNothing().when(orderService).cancelOrder(orderId, email);

      mockMvc.perform(post("/orders/{id}/cancel", orderId).with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/orders"));
    }
  }

  @Nested
  class ConfirmOrder {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testConfirmOrderWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {

      long orderId = 1L;

      mockMvc.perform(post("/orders/{id}/confirm", orderId).with(csrf()))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE", username = "test@test.com")
    void testConfirmOrderWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {

      long orderId = 1L;
      String email = "test@test.com";

      doNothing().when(orderService).confirmOrder(orderId, email);

      mockMvc.perform(post("/orders/{id}/confirm", orderId).with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/orders"));
    }
  }
}
