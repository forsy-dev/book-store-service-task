package com.forsy.service.impl;

import com.forsy.dto.AddToCartDto;
import com.forsy.dto.BookDto;
import com.forsy.dto.CartItemDisplayDto;
import com.forsy.exception.NotFoundException;
import com.forsy.service.BookService;
import com.forsy.service.CartService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link CartService} for managing session-based shopping carts.
 *
 * <p>This service provides the concrete logic for manipulating cart state stored
 * in-memory (typically within a user session). It coordinates with {@link BookService}
 * to ensure data consistency between the transient cart and the persistent inventory.
 *
 * @author Illia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

  private final BookService bookService;
  private final MessageSource messageSource;

  /**
   * {@inheritDoc}
   *
   * <p>Increments the quantity if the book already exists in the cart.
   * Validates the book title via {@code bookService} before addition.
   *
   * @throws IllegalArgumentException if the cart map is null
   */
  @Override
  public void addBookToCart(Map<String, Integer> cart, AddToCartDto dto) {
    if (cart == null) {
      log.error("Attempted to add book to a null cart");
      String message = messageSource.getMessage(
          "error.cart.null", new Object[]{}, LocaleContextHolder.getLocale());
      throw new IllegalArgumentException(message);
    }

    // Ensure the book exists in the database before adding to cart
    bookService.getBookByName(dto.getBookName());

    cart.put(dto.getBookName(), cart.getOrDefault(dto.getBookName(), 0) + dto.getQuantity());
    log.info("Added {} of {} to cart", dto.getQuantity(), dto.getBookName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeBookFromCart(Map<String, Integer> cart, String bookName) {
    if (cart != null && cart.containsKey(bookName)) {
      cart.remove(bookName);
      log.info("Removed {} from cart", bookName);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Gracefully handles scenarios where a book remains in a user's session
   * but has been removed from the persistent store by skipping the invalid
   * entry and logging a warning.
   */
  @Override
  public List<CartItemDisplayDto> getCartItems(Map<String, Integer> cart) {
    List<CartItemDisplayDto> cartItems = new ArrayList<>();

    if (cart == null || cart.isEmpty()) {
      return cartItems;
    }

    for (Map.Entry<String, Integer> entry : cart.entrySet()) {
      String bookName = entry.getKey();
      Integer quantity = entry.getValue();
      try {
        BookDto book = bookService.getBookByName(bookName);
        BigDecimal subtotal = book.getPrice().multiply(BigDecimal.valueOf(quantity));
        cartItems.add(new CartItemDisplayDto(book, quantity, subtotal));
      } catch (NotFoundException e) {
        log.warn("Book '{}' found in session cart but not in database. Skipping.", bookName);
      }
    }
    return cartItems;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Calculates the sum of all item subtotals using a sequential stream.
   */
  @Override
  public BigDecimal calculateTotalCost(List<CartItemDisplayDto> items) {
    return items.stream()
        .map(CartItemDisplayDto::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
