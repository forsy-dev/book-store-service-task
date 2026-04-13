package com.forsy.exception;

/**
 * Exception thrown when a requested resource cannot be found in the system.
 *
 * <p>This runtime exception is utilized across the service layer to signal
 * that a specific entity—such as a book, client, or employee—associated
 * with a given identifier does not exist within the application's persistent
 * storage.
 *
 * @author Illia
 */
public class NotFoundException extends RuntimeException {

  /**
   * Constructs a new NotFoundException with the specified detail message.
   *
   * @param message the detail message explaining which resource was missing
   */
  public NotFoundException(String message) {
    super(message);
  }
}
