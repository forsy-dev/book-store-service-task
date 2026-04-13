package com.forsy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing a specific quantity of a book within an order.
 *
 * <p>Used to encapsulate the relationship between a book title and the number
 * of copies requested, typically as part of a larger order submission or
 * display manifest.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookItemDto {

  /**
   * The name or title of the book in the manifest.
   */
  private String bookName;

  /**
   * The number of copies associated with this specific entry.
   */
  private Integer quantity;
}
