package com.forsy.exception;

/**
 * Exception thrown when a provided password does not match the stored credentials.
 *
 * <p>This runtime exception is primarily used during identity verification
 * processes, such as password updates, to signal that the user's current
 * password was entered incorrectly, preventing unauthorized security changes.
 *
 * @author Illia
 */
public class InvalidPasswordException extends RuntimeException {

  /**
   * Constructs a new InvalidPasswordException with the specified detail message.
   *
   * @param message the detail message explaining the specific validation failure
   */
  public InvalidPasswordException(String message) {
    super(message);
  }
}
