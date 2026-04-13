package com.forsy.model.enums;

/**
 * Enumeration representing the possible states of an order within the system.
 *
 * <p>This lifecycle tracker defines the current processing stage of a bookstore
 * transaction, guiding the business logic for fulfillment, payment, and
 * inventory management.
 *
 * @author Illia
 */
public enum OrderStatus {

  /**
   * The order has been submitted but is awaiting confirmation or payment.
   */
  PENDING,

  /**
   * The order has been verified and is ready for fulfillment or has been completed.
   */
  CONFIRMED,

  /**
   * The order has been voided and will not be processed further.
   */
  CANCELED
}
