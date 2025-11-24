package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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


}
