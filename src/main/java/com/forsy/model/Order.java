package com.forsy.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Persistent entity representing a finalized purchase within the bookstore.
 *
 * <p>This class serves as the central record for a transaction, linking a
 * {@link Client} with the {@link Employee} who processed the request. It
 * maintains the total financial value, the timestamp of the event, and
 * orchestrates the collection of individual {@link BookItem} entries through
 * a composition relationship.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ORDERS")
@Builder
public class Order {

  /**
   * The unique primary key for the order entity.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The client who initiated and funded the purchase.
   *
   * <p>Utilizes lazy fetching to avoid unnecessary loading of sensitive
   * client financial data when only order metadata is required.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  @NotNull(message = "{NotNull.invalid}")
  @ToString.Exclude
  private Client client;

  /**
   * The employee responsible for managing and fulfilling the order.
   *
   * <p>Mapped with lazy fetching to streamline bulk order reporting.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", nullable = false)
  @NotNull(message = "{NotNull.invalid}")
  @ToString.Exclude
  private Employee employee;

  /**
   * The specific date and time when the transaction was officially recorded.
   *
   * <p>Must be a timestamp from the past or the current moment.
   */
  @Column(name = "ORDER_DATE", nullable = false)
  @NotNull(message = "{NotNull.invalid}")
  @PastOrPresent(message = "{PastOrPresent.invalid}")
  private LocalDateTime orderDate;

  /**
   * The total calculated price for the entire order.
   *
   * <p>Must be a positive value of at least 0.01.
   */
  @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
  @NotNull(message = "{NotNull.invalid}")
  @DecimalMin(value = "0.01", message = "{DecimalMin.invalid}")
  private BigDecimal price;

  /**
   * The detailed collection of books and quantities included in this order.
   *
   * <p>Configured with {@link CascadeType#ALL} to ensure that book items are
   * managed automatically in synchronization with the parent order's lifecycle.
   */
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @NotEmpty(message = "{NotEmpty.Order.bookItems}")
  @ToString.Exclude
  private List<BookItem> bookItems;
}
