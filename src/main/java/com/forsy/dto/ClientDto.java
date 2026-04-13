package com.forsy.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing a comprehensive client profile.
 *
 * <p>This DTO is used to transport the full state of a client record, including
 * their identity, credentials, and financial balance. It serves as a general-purpose
 * data container for internal service operations.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto {

  /**
   * The unique email address used to identify the client.
   */
  private String email;

  /**
   * The encrypted or raw security credential for the account.
   */
  private String password;

  /**
   * The full name or display name of the client.
   */
  private String name;

  /**
   * The current amount of funds held in the client's account.
   */
  private BigDecimal balance;
}
