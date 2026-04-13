package com.forsy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Persistent entity representing a specific line item within a customer's order.
 *
 * <p>This class acts as a join entity between {@link Order} and {@link Book},
 * capturing the specific quantity of a book purchased in a single transaction.
 * It maintains data integrity through relational constraints and ensures that
 * order summaries can be reconstructed accurately.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BOOK_ITEMS")
@Builder
public class BookItem {

  /**
   * The unique primary key for the book item entry.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The parent order to which this specific line item belongs.
   *
   * <p>Utilizes lazy fetching to optimize performance during database
   * retrieval.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  @NotNull(message = "{NotNull.invalid}")
  @ToString.Exclude
  private Order order;

  /**
   * The specific book title included in this order entry.
   *
   * <p>Utilizes lazy fetching to prevent unnecessary book metadata
   * loading during bulk order processing.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  @NotNull(message = "{NotNull.invalid}")
  private Book book;

  /**
   * The number of copies of the specific book requested in this item.
   *
   * <p>Must be a positive integer of at least 1.
   */
  @Column(name = "QUANTITY", nullable = false)
  @NotNull(message = "{NotNull.invalid}")
  @Min(value = 1, message = "{Min.invalid}")
  private Integer quantity;
}
