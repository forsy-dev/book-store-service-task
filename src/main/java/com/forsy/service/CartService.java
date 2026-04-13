package com.forsy.service;

import com.forsy.dto.AddToCartDto;
import com.forsy.dto.CartItemDisplayDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service interface defining the business logic for shopping cart management.
 *
 * <p>This service manages the transient state of a user's potential purchases,
 * handling the addition and removal of items, and preparing cart data for
 * presentation. It acts as the logic layer between the user's session-based
 * choices and the final order fulfillment process.
 *
 * @author Illia
 */
public interface CartService {

  /**
   * Adds a specified book and its quantity to the current shopping cart.
   *
   * <p>If the book already exists in the cart, the new quantity is appended
   * to the existing amount. This method should validate the book's existence
   * in the catalog before modification.
   *
   * @param cart the current cart state represented as a map of book names to quantities
   * @param dto  the data transfer object containing the book name and quantity to add
   * @throws IllegalArgumentException if the provided cart map is null
   * @throws com.forsy.exception.NotFoundException if the book to be added does not exist
   */
  void addBookToCart(Map<String, Integer> cart, AddToCartDto dto);

  /**
   * Removes a specific book entirely from the shopping cart.
   *
   * <p>If the book does not exist in the cart, the operation completes silently.
   *
   * @param cart     the current cart state
   * @param bookName the unique title of the book to be removed
   */
  void removeBookFromCart(Map<String, Integer> cart, String bookName);

  /**
   * Transforms the raw cart data into a list of display-ready objects.
   *
   * <p>Enriches the simple name-quantity mapping with detailed book metadata
   * (such as unit price and author) and calculates subtotals for each line item.
   *
   * @param cart the raw map of books and quantities
   * @return a {@link List} of {@link CartItemDisplayDto} for user interface rendering
   */
  List<CartItemDisplayDto> getCartItems(Map<String, Integer> cart);

  /**
   * Calculates the cumulative price of all items currently in the cart.
   *
   * <p>Determines the final financial weight of the traveler's choices
   * by summing the subtotals of all provided display items.
   *
   * @param items the list of prepared cart items with their individual subtotals
   * @return the total cost as a {@link BigDecimal}
   */
  BigDecimal calculateTotalCost(List<CartItemDisplayDto> items);
}
