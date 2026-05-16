package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.forsy.dto.AddToCartDto;
import com.forsy.dto.CartItemDisplayDto;
import com.forsy.service.CartService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
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
class CartSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CartService cartService;

  @Nested
  class AddBookToCart {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testAddBookToCartWhenAuthenticatedAsClientShouldAllowAccess() throws Exception {

      String bookName = "book";
      int quantity = 10;
      AddToCartDto dto = new AddToCartDto(bookName, quantity);

      doNothing().when(cartService).addBookToCart(anyMap(), any(AddToCartDto.class));

      mockMvc.perform(post("/cart/add")
                          .flashAttr("addToCartDTO", dto)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testAddBookToCartWhenAuthenticatedAsEmployeeShouldForbidAccess() throws Exception {

      String bookName = "book";
      int quantity = 10;
      AddToCartDto dto = new AddToCartDto(bookName, quantity);

      mockMvc.perform(post("/cart/add")
                          .flashAttr("addToCartDTO", dto)
                          .with(csrf()))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class ShowCart {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testShowCartWhenAuthenticatedAsClientShouldAllowAccess() throws Exception {

      List<CartItemDisplayDto> items = Collections.emptyList();
      BigDecimal totalCost = BigDecimal.ZERO;

      when(cartService.getCartItems(anyMap())).thenReturn(items);
      when(cartService.calculateTotalCost(items)).thenReturn(totalCost);

      mockMvc.perform(get("/cart"))
          .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testShowCartWhenAuthenticatedAsEmployeeShouldForbidAccess() throws Exception {

      mockMvc.perform(get("/cart"))
          .andExpect(status().isForbidden());
    }
  }
}
