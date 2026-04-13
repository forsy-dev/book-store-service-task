package com.forsy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used for registering a new client in the system.
 *
 * <p>Carries the essential credentials and personal information required to
 * create a new user account. Enforces strict validation on the email format,
 * password complexity, and name length to ensure data integrity from the
 * moment of account creation.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCreateDto {

  /**
   * The unique email address to be used as the account identifier.
   *
   * <p>Must follow standard email formatting rules.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Email(message = "{Email.invalid}")
  private String email;

  /**
   * The secret password for the new account.
   *
   * <p>Must be at least 8 characters long and contain at least one uppercase
   * letter, one lowercase letter, one digit, and one special character.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "{Pattern.password}")
  @Size(max = 100, message = "{Size.invalid}")
  private String password;

  /**
   * The full name or display name of the new client.
   *
   * <p>Must be between 3 and 255 characters in length.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String name;
}
