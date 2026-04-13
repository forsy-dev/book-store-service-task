package com.forsy.dto;

import com.forsy.model.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used for displaying comprehensive order details.
 *
 * <p>This DTO serves as the primary view-model for order history and tracking.
 * It aggregates identification data, participating parties, financial totals,
 * and the current processing status of a bookstore transaction.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDisplayDto {

  /**
   * The unique database identifier for the order.
   */
  private Long id;

  /**
   * The email address of the client who placed the order.
   */
  private String clientEmail;

  /**
   * The email address of the employee responsible for processing the order.
   */
  private String employeeEmail;

  /**
   * The exact timestamp when the order was finalized.
   */
  private LocalDateTime orderDate;

  /**
   * The total monetary value of the order.
   */
  private BigDecimal price;

  /**
   * The collection of individual book items and quantities included in this order.
   */
  private List<BookItemDto> bookItems;

  /**
   * The current lifecycle state of the order (e.g., PENDING, CONFIRMED, CANCELLED).
   */
  private OrderStatus status;
}
