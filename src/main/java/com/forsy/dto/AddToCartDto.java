package com.forsy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used to request the addition of a book to the shopping cart.
 *
 * <p>Captures the essential identity of the book and the desired quantity.
 * Validation constraints are applied to ensure that the book name is well-formed
 * and the quantity represents a positive selection.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartDto {

  /**
   * The unique name or title of the book to be added.
   *
   * <p>Must not be blank and must be between 3 and 255 characters in length.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String bookName;

  /**
   * The number of copies of the book to be added to the cart.
   *
   * <p>Must be a non-null value and at least 1.
   */
  @NotNull(message = "{NotNull.invalid}")
  @Min(value = 1, message = "{Min.invalid}")
  private Integer quantity;
}
