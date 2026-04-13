package com.forsy.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used for displaying items within the shopping cart.
 *
 * <p>This DTO combines the full book details with the requested quantity and
 * the calculated cost for those specific items, providing a complete view for
 * the user during the checkout process.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDisplayDto {

  /**
   * The detailed information of the book added to the cart.
   */
  private BookDto book;

  /**
   * The number of copies of the book currently in the cart.
   */
  private int quantity;

  /**
   * The total cost for this specific item entry (price multiplied by quantity).
   */
  private BigDecimal subtotal;
}
