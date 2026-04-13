package com.forsy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Persistent entity representing a staff member within the bookstore system.
 *
 * <p>Extends the base {@link User} class to include professional contact
 * information and personal metadata required for employment records. This
 * entity is mapped to the "EMPLOYEES" table and inherits core identity
 * credentials.
 *
 * @author Illia
 */
@Entity
@Table(name = "EMPLOYEES")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Employee extends User {

  /**
   * The primary contact telephone number for the employee.
   *
   * <p>Must follow a valid format of 10 to 20 characters, potentially
   * including symbols such as +, (), or hyphens.
   */
  @Column(name = "PHONE")
  @NotBlank(message = "{NotBlank.invalid}")
  @Pattern(regexp = "^\\+?[0-9\\s()-]{10,20}$", message = "{Pattern.phone}")
  private String phone;

  /**
   * The employee's legal date of birth.
   *
   * <p>Used for age verification and internal personnel records. Must be
   * a date in the past or present.
   */
  @Column(name = "BIRTH_DATE")
  @NotNull(message = "{NotNull.invalid}")
  @PastOrPresent(message = "{PastOrPresent.invalid}")
  private LocalDate birthDate;

  /**
   * Full constructor for the Employee entity.
   *
   * @param id         the unique identifier for the user
   * @param name       the full name of the employee
   * @param email      the unique professional email address
   * @param password   the secured access credential
   * @param phone      the contact phone number
   * @param birthDate  the employee's date of birth
   */
  public Employee(Long id, String name, String email, String password,
                  String phone, LocalDate birthDate) {
    super(id, name, email, password);
    this.phone = phone;
    this.birthDate = birthDate;
  }
}
