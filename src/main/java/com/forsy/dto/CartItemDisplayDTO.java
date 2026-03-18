package com.forsy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDisplayDTO {

    private BookDTO book;
    private int quantity;
    private BigDecimal subtotal;
}
