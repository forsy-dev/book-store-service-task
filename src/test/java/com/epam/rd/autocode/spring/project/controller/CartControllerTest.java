package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CartService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

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
}
