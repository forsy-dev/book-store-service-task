package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.CartItemDisplayDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CartService {

    /**
     * Validates the request and adds the book to the cart map.
     * @param cart The current session cart.
     */
    void addBookToCart(Map<String, Integer> cart, AddToCartDTO dto);

    /**
     * Removes a book from the cart.
     * @param cart The current session cart.
     * @param bookName The name of the book to remove.
     */
//    void removeBookFromCart(Map<String, Integer> cart, String bookName);

    /**
     * Converts the simple Map (Name -> Qty) into a list of detailed DTOs
     * by fetching book details from the database.
     * @param cart The current session cart.
     * @return List of displayable cart items.
     */
    List<CartItemDisplayDTO> getCartItems(Map<String, Integer> cart);

    /**
     * Calculates the total cost of all items in the cart list.
     * @param items The list of display items.
     * @return The total cost.
     */
    BigDecimal calculateTotalCost(List<CartItemDisplayDTO> items);
}
