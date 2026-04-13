package com.forsy.controller;

import com.forsy.conf.jwt.JwtUtils;
import com.forsy.dto.AddToCartDto;
import com.forsy.dto.CartItemDisplayDto;
import com.forsy.exception.NotFoundException;
import com.forsy.service.CartService;
import com.forsy.service.impl.CurrencyService;
import com.forsy.util.CartCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private CurrencyService currencyService;

    @MockBean
    private CartCookieUtil cartCookieUtil;

    @Nested
    class AddBookToCart {

        @Test
        void testAddBookToCart_ShouldRedirect() throws Exception {
            String bookName = "book";
            int quantity = 10;
            AddToCartDto dto = new AddToCartDto(bookName, quantity);

            doNothing().when(cartService).addBookToCart(anyMap(), any(AddToCartDto.class));

            mockMvc.perform(post("/cart/add")
                            .flashAttr("addToCartDTO", dto)
                            .with(user("testuser").roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }

        @Test
        void testAddBookToCart_ShouldRedirectWhenValidationFails() throws Exception {
            String bookName = "book";
            int quantity = 0;
          AddToCartDto dto = new AddToCartDto(bookName, quantity);

            mockMvc.perform(post("/cart/add")
                            .flashAttr("addToCartDTO", dto)
                            .with(user("testuser").roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }

        @Test
        void testAddBookToCart_ShouldReturnErrorPageWhenBookNotFound() throws Exception {
            String bookName = "book";
            int quantity = 1;
          AddToCartDto dto = new AddToCartDto(bookName, quantity);

            doThrow(NotFoundException.class).when(cartService).addBookToCart(anyMap(), any(AddToCartDto.class));

            mockMvc.perform(post("/cart/add")
                            .flashAttr("addToCartDTO", dto)
                            .with(user("testuser").roles("CLIENT"))
                            .with(csrf()))
                    .andExpect(view().name("error"));
        }
    }

    @Nested
    class ShowCart {

        @Test
        void testShowCart_ShouldReturnPage() throws Exception {
            List<CartItemDisplayDto> items = Collections.emptyList();
            BigDecimal totalCost = BigDecimal.ZERO;

            when(cartService.getCartItems(anyMap())).thenReturn(items);
            when(cartService.calculateTotalCost(items)).thenReturn(totalCost);

            mockMvc.perform(get("/cart")
                            .with(user("testuser").roles("CLIENT")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cart"));
        }
    }

    @Nested
    class RemoveBookFromCart {

        @Test
        void testRemoveBookFromCart_ShouldRemoveItemAndRedirect() throws Exception {
            String bookName = "book1";
            Map<String, Integer> cart = new HashMap<>();
            cart.put(bookName, 1);

            when(cartCookieUtil.getCartFromCookie(any(HttpServletRequest.class))).thenReturn(cart);
            doNothing().when(cartCookieUtil).saveCartToCookie(any(HttpServletResponse.class), anyMap());

            mockMvc.perform(post("/cart/remove")
                    .param("bookName", bookName)
                    .with(user("client").roles("CLIENT"))
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

            verify(cartService).removeBookFromCart(cart, bookName);
            verify(cartCookieUtil).saveCartToCookie(any(HttpServletResponse.class), eq(cart));
        }
    }
}
