package com.forsy.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used for displaying employee profile information.
 *
 * <p>Provides a read-only view of an employee's professional identity,
 * including their contact details and administrative metadata. This DTO is
 * used within the secure employee dashboard and for internal staff directories.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDisplayDto {

  /**
   * The unique corporate email address identifying the employee.
   */
  private String email;

  /**
   * The full name or display name of the employee.
   */
  private String name;

  /**
   * The employee's date of birth.
   */
  private LocalDate birthDate;

  /**
   * The primary contact phone number for the employee.
   */
  private String phone;
}
