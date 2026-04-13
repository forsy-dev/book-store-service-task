package com.forsy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used for adding funds to a client's account balance.
 *
 * <p>This DTO ensures that the deposit amount is both present and meets the
 * minimum required threshold for a valid transaction.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBalanceDto {

  /**
   * The amount of money to be added to the account.
   *
   * <p>Must be a positive value, with a minimum allowed deposit of 0.01.
   */
  @NotNull(message = "{NotNull.invalid}")
  @DecimalMin(value = "0.01", message = "{DecimalMin.invalid}")
  private BigDecimal amount;
}
