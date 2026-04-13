package com.forsy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing the core data of a bookstore order.
 *
 * <p>This DTO serves as a general-purpose container for order information
 * within the service layer. It encapsulates the relationship between the
 * purchasing client, the processing employee, and the collection of books
 * included in the transaction.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

  /**
   * The unique email address of the client who placed the order.
   */
  private String clientEmail;

  /**
   * The unique email address of the employee responsible for the order.
   */
  private String employeeEmail;

  /**
   * The date and time when the order record was created.
   */
  private LocalDateTime orderDate;

  /**
   * The total monetary value of the transaction.
   */
  private BigDecimal price;

  /**
   * The list of books and their respective quantities included in this order.
   */
  private List<BookItemDto> bookItems;
}
