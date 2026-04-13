package com.forsy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used for updating an existing client's profile information.
 *
 * <p>This DTO is specifically designed for self-service profile updates,
 * allowing clients to modify their display name while keeping other sensitive
 * account identifiers immutable during this operation.
 *
 * @author Illia
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientUpdateDto {

  /**
   * The new full name or display name for the client.
   *
   * <p>Must not be blank and must be between 3 and 255 characters in length.
   */
  @NotBlank(message = "{NotBlank.invalid}")
  @Size(min = 3, max = 255, message = "{Size.invalid}")
  private String name;
}
