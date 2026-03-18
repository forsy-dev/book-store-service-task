package com.forsy.service;

import com.forsy.dto.AddToCartDTO;
import com.forsy.dto.CartItemDisplayDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CartService {

    void addBookToCart(Map<String, Integer> cart, AddToCartDTO dto);

    void removeBookFromCart(Map<String, Integer> cart, String bookName);

    List<CartItemDisplayDTO> getCartItems(Map<String, Integer> cart);

    BigDecimal calculateTotalCost(List<CartItemDisplayDTO> items);
}
