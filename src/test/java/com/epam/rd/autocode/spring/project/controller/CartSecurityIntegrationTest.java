package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.CartService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CartSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Nested
    class AddBookToCart {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testAddBookToCart_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {

            String bookName = "book";
            int quantity = 10;
            AddToCartDTO dto = new AddToCartDTO(bookName, quantity);

            doNothing().when(cartService).addBookToCart(anyMap(), any(AddToCartDTO.class));

            mockMvc.perform(post("/cart/add")
                            .flashAttr("addToCartDTO", dto))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books"));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testAddBookToCart_WhenAuthenticatedAsEmployee_ShouldForbidAccess() throws Exception {

            String bookName = "book";
            int quantity = 10;
            AddToCartDTO dto = new AddToCartDTO(bookName, quantity);

            mockMvc.perform(post("/cart/add")
                            .flashAttr("addToCartDTO", dto))
                    .andExpect(status().isForbidden());
        }
    }
}
