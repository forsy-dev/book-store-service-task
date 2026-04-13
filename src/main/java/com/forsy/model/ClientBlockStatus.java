package com.forsy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistent entity representing the administrative block status of a client.
 *
 * <p>This entity acts as a security ledger, tracking whether a specific user
 * (identified by their unique email) is currently prohibited from performing
 * transactions or accessing certain bookstore services.
 *
 * @author Illia
 */
@Entity
@Table(name = "CLIENT_BLOCK_STATUS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientBlockStatus {

  /**
   * The unique primary key for the block status record.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The unique email address of the client associated with this status.
   *
   * <p>Serves as the link between the core user credentials and their
   * current standing within the system.
   */
  @Column(name = "CLIENT_EMAIL", nullable = false, unique = true)
  private String clientEmail;

  /**
   * Indicates whether the client is currently barred from system activities.
   *
   * <p>Defaults to {@code false} to ensure that new clients are granted
   * immediate access upon registration.
   */
  @Column(name = "IS_BLOCKED", nullable = false)
  @Builder.Default
  private boolean isBlocked = false;
}
