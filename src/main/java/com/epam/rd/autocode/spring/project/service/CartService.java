package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.CartItemDisplayDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CartService {

    void addBookToCart(Map<String, Integer> cart, AddToCartDTO dto);

    void removeBookFromCart(Map<String, Integer> cart, String bookName);

    List<CartItemDisplayDTO> getCartItems(Map<String, Integer> cart);

    BigDecimal calculateTotalCost(List<CartItemDisplayDTO> items);
}
