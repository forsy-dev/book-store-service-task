package com.forsy.model;

import com.forsy.model.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistent entity representing the current processing state of an order.
 *
 * <p>This entity acts as a specialized registry, mapping a specific order
 * identifier to its lifecycle status (e.g., PENDING, CONFIRMED). By
 * maintaining this as a separate record, the system can quickly query and
 * update order states without loading the entire transaction graph.
 *
 * @author Illia
 */
@Entity
@Table(name = "ORDER_STATUS_REGISTRY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusRecord {

  /**
   * The unique primary key for the status registry entry.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The unique identifier of the order associated with this status record.
   *
   * <p>Serves as the link to the core {@link Order} entity.
   */
  @Column(name = "ORDER_ID", nullable = false, unique = true)
  private Long orderId;

  /**
   * The current state of the associated order.
   *
   * <p>Stored as a string representation of the {@link OrderStatus} enum.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  private OrderStatus status;
}
