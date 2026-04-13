package com.forsy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing the full details of an employee.
 *
 * <p>Used for internal operations requiring the complete employee record,
 * including security credentials and contact information. Enforces strict
 * validation rules for corporate identity and account security.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {

  /**
   * The unique corporate email address identifying the employee.
   *
   * <p>Must be a valid email format and cannot be blank.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Email(message = "{Email.invalid}")
  private String email;

  /**
   * The secret security credential for the employee account.
   *
   * <p>Must be at least 8 characters long and contain at least one uppercase
   * letter, one lowercase letter, one digit, and one special character.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(max = 100, message = "{Size.invalid}")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "{Pattern.password}")
  private String password;

  /**
   * The full name or display name of the employee.
   *
   * <p>Must be between 3 and 255 characters in length.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String name;

  /**
   * The employee's date of birth.
   *
   * <p>Must be a non-null value representing a date in the past or present.
   */
  @NotNull(message = "{NotNull.invalid}")
  @PastOrPresent(message = "{PastOrPresent.invalid}")
  private LocalDate birthDate;

  /**
   * The primary contact phone number for the employee.
   *
   * <p>Must follow a standard international or local phone format (10-20 digits).
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Pattern(regexp = "^\\+?[0-9\\s()-]{10,20}$", message = "{Pattern.phone}")
  private String phone;
}
