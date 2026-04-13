package com.forsy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used for updating a user's security credentials.
 *
 * <p>This DTO handles the transition from an old password to a new one,
 * enforcing strict complexity requirements for the new password to ensure
 * the continued security of user accounts.
 *
 * @author Illia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordDto {

  /**
   * The user's current password, used for identity verification.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  private String oldPassword;

  /**
   * The new password to be set for the account.
   *
   * <p>Must be at least 8 characters long and contain at least one uppercase
   * letter, one lowercase letter, one digit, and one special character.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(max = 100, message = "{Size.invalid}")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "{Pattern.password}")
  private String newPassword;
}
