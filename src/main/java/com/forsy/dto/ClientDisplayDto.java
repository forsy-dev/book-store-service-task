package com.forsy.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used for displaying client information.
 *
 * <p>Provides a read-only view of a client's profile, including their identity,
 * financial standing, and account status. This DTO is typically used in
 * administrative dashboards and user profile headers.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDisplayDto {

  /**
   * The unique email address used to identify the client account.
   */
  private String email;

  /**
   * The full name or display name of the client.
   */
  private String name;

  /**
   * The current available funds in the client's account treasury.
   */
  private BigDecimal balance;

  /**
   * A flag indicating whether the client's access to the system is restricted.
   *
   * <p>Returns {@code true} if the account is blocked, {@code false} otherwise.
   */
  private Boolean isBlocked;
}
