package com.forsy.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base abstraction for all identifiable individuals within the bookstore system.
 *
 * <p>As a {@link MappedSuperclass}, this class provides common fields such as
 * identity, contact, and security credentials to its subclasses (e.g., Client,
 * Employee) without being mapped to its own database table. It ensures that
 * core user attributes are consistently managed and validated across the
 * entire domain.
 *
 * @author Illia
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User {

  /**
   * The unique primary key identifier for the user.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The full name or display name of the user.
   *
   * <p>Must be between 3 and 255 characters and cannot be blank.
   */
  @Column(name = "NAME", nullable = false)
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String name;

  /**
   * The unique email address used for identification and authentication.
   *
   * <p>This field is immutable once set and must follow a valid email format.
   */
  @Column(name = "EMAIL", nullable = false, unique = true, updatable = false)
  @NotBlank(message = "{NotBlank.invalid}")
  @Email(message = "{Email.invalid}")
  private String email;

  /**
   * The encoded security credential used for system access.
   */
  @Column(name = "PASSWORD")
  @NotBlank(message = "{NotBlank.invalid}")
  private String password;
}
