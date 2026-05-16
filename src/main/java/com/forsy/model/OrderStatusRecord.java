package com.forsy.model;

import com.forsy.model.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Справжній зв'язок з об'єктом Order.
   * Тепер Hibernate розуміє, що це зовнішній ключ (Foreign Key).
   */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  @NotNull(message = "{NotNull.invalid}")
  private Order order;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  private OrderStatus status;
}
