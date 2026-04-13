package com.forsy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used to request the creation of a new order.
 *
 * <p>Encapsulates all necessary information to finalize a transaction, including
 * the identities of the participating client and assigned employee, the exact
 * timing of the request, and the collection of books being purchased.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {

  /**
   * The unique email address of the client placing the order.
   *
   * <p>Must be a valid email format and cannot be blank.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Email(message = "{Email.invalid}")
  private String clientEmail;

  /**
   * The unique email address of the employee assigned to process this order.
   *
   * <p>Must be a valid email format and cannot be blank.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Email(message = "{Email.invalid}")
  private String employeeEmail;

  /**
   * The date and time when the order was officially placed.
   *
   * <p>Must be a non-null value representing a time in the past or present.
   */
  @NotNull(message = "{NotNull.invalid}")
  @PastOrPresent(message = "{PastOrPresent.invalid}")
  private LocalDateTime orderDate;

  /**
   * The list of specific book items and their quantities included in the order.
   *
   * <p>The list must contain at least one item to form a valid order request.
   */
  @NotEmpty(message = "{NotEmpty.Order.bookItems}")
  private List<BookItemDto> bookItems;
}

