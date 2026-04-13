package com.forsy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.forsy.dto.AddToCartDto;
import com.forsy.dto.BookDto;
import com.forsy.dto.CartItemDisplayDto;
import com.forsy.exception.NotFoundException;
import com.forsy.service.BookService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

  @InjectMocks
  private CartServiceImpl cartService;

  @Mock
  private BookService bookService;

  @Mock
  private MessageSource messageSource;

  @Nested
  class AddBookToCart {

    @Test
    void testAddBookToCartShouldReturnNothingWhenSuccess() {
      Map<String, Integer> cart = new HashMap<>();
      String bookName = "book";
      int quantity = 1;
      AddToCartDto dto = new AddToCartDto(bookName, quantity);

      when(bookService.getBookByName(bookName)).thenReturn(new BookDto());

      cartService.addBookToCart(cart, dto);

      verify(bookService, times(1)).getBookByName(bookName);
    }

    @Test
    void testAddBookToCartShouldThrowExceptionWhenCartIsNull() {
      String bookName = "book";
      int quantity = 1;
      AddToCartDto dto = new AddToCartDto(bookName, quantity);

      when(messageSource.getMessage(eq("error.cart.null"), any(), any(Locale.class)))
          .thenReturn("Cart is null");

      assertThrows(IllegalArgumentException.class, () -> cartService.addBookToCart(null, dto));

      verify(bookService, never()).getBookByName(bookName);
    }

    @Test
    void testAddBookToCartShouldThrowExceptionWhenBookNotFound() {
      Map<String, Integer> cart = new HashMap<>();
      String bookName = "book";
      int quantity = 1;
      AddToCartDto dto = new AddToCartDto(bookName, quantity);

      when(bookService.getBookByName(bookName)).thenThrow(new NotFoundException("Book not found"));

      assertThrows(NotFoundException.class, () -> cartService.addBookToCart(cart, dto));

      verify(bookService, times(1)).getBookByName(bookName);
    }
  }

  @Nested
  class GetCart {

    @Test
    void testGetCartShouldReturnListWhenSuccess() {
      String bookName = "book";
      int quantity = 2;
      Map<String, Integer> cart = new HashMap<>();
      cart.put(bookName, quantity);

      BigDecimal price = BigDecimal.TEN;
      BookDto bookDto = BookDto.builder().name(bookName).price(price).build();
      final BigDecimal expectedSubtotal = price.multiply(BigDecimal.valueOf(quantity));

      when(bookService.getBookByName(bookName)).thenReturn(bookDto);

      List<CartItemDisplayDto> dto = cartService.getCartItems(cart);

      assertEquals(bookName, dto.get(0).getBook().getName());
      assertEquals(quantity, dto.get(0).getQuantity());
      assertEquals(0, expectedSubtotal.compareTo(
          dto.get(0).getSubtotal()), "Subtotal calculation is incorrect");

      verify(bookService, times(1)).getBookByName(bookName);
    }

    @Test
    void testGetCartShouldReturnEmptyListWhenCartIsNull() {

      List<CartItemDisplayDto> dto = cartService.getCartItems(null);

      assertEquals(0, dto.size());

      verify(bookService, never()).getBookByName(anyString());
    }
  }

  @Nested
  class GetCartItems {

    @Test
    void testGetCartItemsShouldReturnItems() {
      String bookName = "book";
      int quantity = 2;
      Map<String, Integer> cart = new HashMap<>();
      cart.put(bookName, quantity);

      BigDecimal price = BigDecimal.TEN;
      BookDto bookDto = BookDto.builder().name(bookName).price(price).build();
      final BigDecimal expectedSubtotal = price.multiply(BigDecimal.valueOf(quantity));

      when(bookService.getBookByName(bookName)).thenReturn(bookDto);

      List<CartItemDisplayDto> dto = cartService.getCartItems(cart);

      assertEquals(bookName, dto.get(0).getBook().getName());
      assertEquals(quantity, dto.get(0).getQuantity());
      assertEquals(0, expectedSubtotal.compareTo(
          dto.get(0).getSubtotal()), "Subtotal calculation is incorrect");

      verify(bookService, times(1)).getBookByName(bookName);
    }

    @Test
    void testGetCartItemsShouldSkipItemWhenNotFound() {
      String bookName = "book";
      int quantity = 2;
      Map<String, Integer> cart = new HashMap<>();
      cart.put(bookName, quantity);

      when(bookService.getBookByName(bookName)).thenThrow(new NotFoundException("Book not found"));

      List<CartItemDisplayDto> dto = cartService.getCartItems(cart);

      assertEquals(0, dto.size());

      verify(bookService, times(1)).getBookByName(bookName);
    }
  }

  @Nested
  class CalculateTotalCost {

    @Test
    void testCalculateTotalCostShouldReturnSum() {
      CartItemDisplayDto dto1 = CartItemDisplayDto.builder().subtotal(BigDecimal.TEN).build();
      CartItemDisplayDto dto2 = CartItemDisplayDto.builder().subtotal(BigDecimal.ONE).build();
      List<CartItemDisplayDto> items = Arrays.asList(dto1, dto2);

      BigDecimal total = cartService.calculateTotalCost(items);

      assertEquals(BigDecimal.valueOf(11), total);
    }

    @Test
    void testCalculateTotalCostWhenListEmptyShouldReturnZero() {
      BigDecimal total = cartService.calculateTotalCost(List.of());

      assertEquals(BigDecimal.ZERO, total);
    }
  }

  @Nested
  class RemoveBookFromCart {

    @Test
    void testRemoveBookFromCartShouldRemoveBook() {
      Map<String, Integer> cart = new HashMap<>();
      cart.put("book", 1);
      cartService.removeBookFromCart(cart, "book");
      assertEquals(0, cart.size());
    }
  }
}
