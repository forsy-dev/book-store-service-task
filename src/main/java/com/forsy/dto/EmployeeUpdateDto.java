package com.forsy.dto;

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
 * Data transfer object used for updating an existing employee's profile.
 *
 * <p>This DTO allows employees to modify their personal information such as
 * their display name, contact phone number, and birth date. It enforces
 * validation rules to maintain data quality within the internal guild records.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeUpdateDto {

  /**
   * The updated full name or display name for the employee.
   *
   * <p>Must not be blank and must be between 3 and 255 characters.
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
   * The updated primary contact phone number for the employee.
   *
   * <p>Must follow a valid telephone format consisting of 10 to 20 digits,
   * potentially including symbols such as +, (), or hyphens.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Pattern(regexp = "^\\+?[0-9\\s()-]{10,20}$", message = "{Pattern.phone}")
  private String phone;
}
