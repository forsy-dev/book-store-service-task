package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartItemDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private BookService bookService;

    @Nested
    class AddBookToCart {

        @Test
        void testAddBookToCart_ShouldReturnNothingWhenSuccess() {
            Map<String, Integer> cart = new HashMap<>();
            String bookName = "book";
            int quantity = 1;
            AddToCartDTO dto = new AddToCartDTO(bookName, quantity);

            when(bookService.getBookByName(bookName)).thenReturn(new BookDTO());

            cartService.addBookToCart(cart, dto);

            verify(bookService, times(1)).getBookByName(bookName);
        }

        @Test
        void testAddBookToCart_ShouldThrowExceptionWhenCartIsNull() {
            Map<String, Integer> cart = null;
            String bookName = "book";
            int quantity = 1;
            AddToCartDTO dto = new AddToCartDTO(bookName, quantity);

            assertThrows(IllegalArgumentException.class, () -> cartService.addBookToCart(cart, dto));

            verify(bookService, never()).getBookByName(bookName);
        }

        @Test
        void testAddBookToCart_ShouldThrowExceptionWhenBookNotFound() {
            Map<String, Integer> cart = new HashMap<>();
            String bookName = "book";
            int quantity = 1;
            AddToCartDTO dto = new AddToCartDTO(bookName, quantity);

            when(bookService.getBookByName(bookName)).thenThrow(new NotFoundException("Book not found"));

            assertThrows(NotFoundException.class, () -> cartService.addBookToCart(cart, dto));

            verify(bookService, times(1)).getBookByName(bookName);
        }
    }

    @Nested
    class GetCart {

        @Test
        void testGetCart_ShouldReturnListWhenSuccess() {
            String bookName = "book";
            int quantity = 2;
            Map<String, Integer> cart = new HashMap<>();
            cart.put(bookName, quantity);

            BigDecimal price = BigDecimal.TEN;
            BookDTO bookDto = BookDTO.builder().name(bookName).price(price).build();
            BigDecimal expectedSubtotal = price.multiply(BigDecimal.valueOf(quantity));

            when(bookService.getBookByName(bookName)).thenReturn(bookDto);

            List<CartItemDisplayDTO> dto = cartService.getCartItems(cart);

            assertEquals(bookName, dto.get(0).getBook().getName());
            assertEquals(quantity, dto.get(0).getQuantity());
            assertEquals(0, expectedSubtotal.compareTo(dto.get(0).getSubtotal()), "Subtotal calculation is incorrect");

            verify(bookService, times(1)).getBookByName(bookName);
        }

        @Test
        void testGetCart_ShouldReturnEmptyListWhenCartIsNull() {
            Map<String, Integer> cart = null;

            List<CartItemDisplayDTO> dto = cartService.getCartItems(cart);

            assertEquals(0, dto.size());

            verify(bookService, never()).getBookByName(anyString());
        }
    }

    @Nested
    class GetCartItems {

        @Test
        void testGetCartItems_ShouldReturnItems() {
            String bookName = "book";
            int quantity = 2;
            Map<String, Integer> cart = new HashMap<>();
            cart.put(bookName, quantity);

            BigDecimal price = BigDecimal.TEN;
            BookDTO bookDto = BookDTO.builder().name(bookName).price(price).build();
            BigDecimal expectedSubtotal = price.multiply(BigDecimal.valueOf(quantity));

            when(bookService.getBookByName(bookName)).thenReturn(bookDto);

            List<CartItemDisplayDTO> dto = cartService.getCartItems(cart);

            assertEquals(bookName, dto.get(0).getBook().getName());
            assertEquals(quantity, dto.get(0).getQuantity());
            assertEquals(0, expectedSubtotal.compareTo(dto.get(0).getSubtotal()), "Subtotal calculation is incorrect");

            verify(bookService, times(1)).getBookByName(bookName);
        }

        @Test
        void testGetCartItems_ShouldSkipItem_WhenNotFound() {
            String bookName = "book";
            int quantity = 2;
            Map<String, Integer> cart = new HashMap<>();
            cart.put(bookName, quantity);

            when(bookService.getBookByName(bookName)).thenThrow(new NotFoundException("Book not found"));

            List<CartItemDisplayDTO> dto = cartService.getCartItems(cart);

            assertEquals(0, dto.size());

            verify(bookService, times(1)).getBookByName(bookName);
        }
    }

    @Nested
    class CalculateTotalCost {

        @Test
        void testCalculateTotalCost_ShouldReturnSum() {
            CartItemDisplayDTO dto1 = CartItemDisplayDTO.builder().subtotal(BigDecimal.TEN).build();
            CartItemDisplayDTO dto2 = CartItemDisplayDTO.builder().subtotal(BigDecimal.ONE).build();
            List<CartItemDisplayDTO> items = Arrays.asList(dto1, dto2);

            BigDecimal total = cartService.calculateTotalCost(items);

            assertEquals(BigDecimal.valueOf(11), total);
        }

        @Test
        void testCalculateTotalCost_WhenListEmpty_ShouldReturnZero() {
            BigDecimal total = cartService.calculateTotalCost(Arrays.asList());

            assertEquals(BigDecimal.ZERO, total);
        }
    }
}
