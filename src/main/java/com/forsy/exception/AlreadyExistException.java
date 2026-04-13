package com.forsy.exception;

/**
 * Exception thrown when an attempt is made to create a resource that already exists.
 *
 * <p>This runtime exception is commonly used during registration or data entry
 * processes to signal that a unique identifier (such as an email address or
 * a book title) is already present in the system's archives, preventing
 * duplicate records.
 *
 * @author Illia
 */
public class AlreadyExistException extends RuntimeException {

  /**
   * Constructs a new AlreadyExistException with the specified detail message.
   *
   * @param message the detail message explaining which resource already exists
   */
  public AlreadyExistException(String message) {
    super(message);
  }
}
