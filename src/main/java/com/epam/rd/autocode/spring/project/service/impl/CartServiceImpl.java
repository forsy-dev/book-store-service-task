package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartItemDisplayDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final BookService bookService;

    @Override
    public void addBookToCart(Map<String, Integer> cart, AddToCartDTO dto) {
        // 1. Validate Cart (Must not be null for a write operation)
        if (cart == null) {
            log.error("Attempted to add book to a null cart");
            throw new IllegalArgumentException("Cart cannot be null");
        }

        // 2. Validate Book Existence
        // This will throw NotFoundException if the book doesn't exist
        bookService.getBookByName(dto.getBookName());

        // 3. Add to map
        cart.put(dto.getBookName(), cart.getOrDefault(dto.getBookName(), 0) + dto.getQuantity());
        log.info("Added {} of {} to cart", dto.getBookName(), dto.getQuantity());
    }

    @Override
    public void removeBookFromCart(Map<String, Integer> cart, String bookName) {
        if (cart != null && cart.containsKey(bookName)) {
            cart.remove(bookName);
            log.info("Removed {} from cart", bookName);
        }
    }

    @Override
    public List<CartItemDisplayDTO> getCartItems(Map<String, Integer> cart) {
        List<CartItemDisplayDTO> cartItems = new ArrayList<>();

        if (cart == null || cart.isEmpty()) {
            return cartItems;
        }

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String bookName = entry.getKey();
            Integer quantity = entry.getValue();
            try {
                BookDTO book = bookService.getBookByName(bookName);
                BigDecimal subtotal = book.getPrice().multiply(BigDecimal.valueOf(quantity));
                cartItems.add(new CartItemDisplayDTO(book, quantity, subtotal));
            } catch (NotFoundException e) {
                log.warn("Book {} found in session cart but not in database. Skipping.", bookName);
                // Optional: You could remove it from the map here to clean up the session
            }
        }
        return cartItems;
    }

    @Override
    public BigDecimal calculateTotalCost(List<CartItemDisplayDTO> items) {
        return items.stream()
                .map(CartItemDisplayDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
