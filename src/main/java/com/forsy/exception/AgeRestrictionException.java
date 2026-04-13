package com.forsy.exception;

/**
 * Exception thrown when a client does not meet the age requirements for a book.
 *
 * <p>This runtime exception is utilized by the order and purchase services to
 * enforce age-based content restrictions, ensuring that clients can only
 * purchase materials appropriate for their documented age group.
 *
 * @author Illia
 */
public class AgeRestrictionException extends RuntimeException {

  /**
   * Constructs a new AgeRestrictionException with the specified detail message.
   *
   * @param message the detail message explaining the specific age restriction violation
   */
  public AgeRestrictionException(String message) {
    super(message);
  }
}
