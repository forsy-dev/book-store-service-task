package com.forsy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Persistent entity representing a customer within the bookstore system.
 *
 * <p>Extends the base {@link User} class to include financial attributes
 * specific to clients, such as their current account balance. This entity
 * is mapped to the "CLIENTS" table and utilizes inheritance to maintain
 * shared identity credentials.
 *
 * @author Illia
 */
@Entity
@Table(name = "CLIENTS")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Client extends User {

  /**
   * The current amount of funds available in the client's account treasury.
   *
   * <p>Defaults to {@link BigDecimal#ZERO} upon initialization to ensure
   * new accounts begin with a clean ledger.
   */
  @Column(name = "BALANCE", nullable = false)
  @Builder.Default
  private BigDecimal balance = BigDecimal.ZERO;

  /**
   * Full constructor for the Client entity.
   *
   * @param id       the unique identifier for the user
   * @param name     the display name of the client
   * @param email    the unique email address used for login
   * @param password the secured password credential
   * @param balance  the starting balance for the client's account
   */
  public Client(Long id, String name, String email, String password, BigDecimal balance) {
    super(id, name, email, password);
    this.balance = balance;
  }
}
