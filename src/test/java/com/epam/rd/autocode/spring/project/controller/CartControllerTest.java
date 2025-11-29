package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.jwt.JwtUtils;
import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.CartItemDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.util.CartCookieUtil;
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
    private CartCookieUtil cartCookieUtil;

    @Nested
    class AddBookToCart {

        @Test
        void testAddBookToCart_ShouldRedirect() throws Exception {
            String bookName = "book";
            int quantity = 10;
            AddToCartDTO dto = new AddToCartDTO(bookName, quantity);

            doNothing().when(cartService).addBookToCart(anyMap(), any(AddToCartDTO.class));

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
            AddToCartDTO dto = new AddToCartDTO(bookName, quantity);

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
            AddToCartDTO dto = new AddToCartDTO(bookName, quantity);

            doThrow(NotFoundException.class).when(cartService).addBookToCart(anyMap(), any(AddToCartDTO.class));

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
            List<CartItemDisplayDTO> items = Collections.emptyList();
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
