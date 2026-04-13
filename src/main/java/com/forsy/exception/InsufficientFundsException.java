package com.forsy.exception;

/**
 * Exception thrown when a client's account balance is inadequate for a transaction.
 *
 * <p>This runtime exception is primarily triggered during the order placement
 * or balance deduction process to signal that the requested operation cannot
 * be completed due to a lack of available funds in the user's treasury.
 *
 * @author Illia
 */
public class InsufficientFundsException extends RuntimeException {

  /**
   * Constructs a new InsufficientFundsException with the specified detail message.
   *
   * @param message the detail message explaining the specific funding shortfall
   */
  public InsufficientFundsException(String message) {
    super(message);
  }
}
