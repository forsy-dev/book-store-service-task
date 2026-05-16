package com.forsy.util;

/**
 * Utility class containing centralized keys for localized messages.
 *
 * <p>This class acts as a central registry for message keys defined in the
 * application's resource bundles (e.g., {@code messages.properties}).
 * Using constants here prevents hard-coding string literals across
 * service and controller layers, ensuring consistency and ease of maintenance
 * for internationalization (i18n).
 *
 * @author Illia
 */
public class MessageKeys {

  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * @throws UnsupportedOperationException if an attempt is made to
   *                                       instantiate this class
   */
  private MessageKeys() {
    throw new UnsupportedOperationException(
        "This is a utility class and cannot be instantiated");
  }

  public static final String ERROR_BOOK_NOT_FOUND = "error.book.not.found";
  public static final String ERROR_BOOK_ALREADY_EXISTS = "error.book.already.exist";
  public static final String PROFILE_UPDATE_SUCCESS_MESSAGE = "profile.update.success.message";
  public static final String CLIENT_BALANCE_ERROR_MESSAGE = "client.balance.error.message";
  public static final String CLIENT_BALANCE_SUCCESS_MESSAGE = "client.balance.success.message";
  public static final String ERROR_USER_NOT_FOUND = "error.user.not.found";
  public static final String ERROR_USER_ALREADY_EXISTS = "error.user.already.exist";
  public static final String ERROR_USER_OLD_PASSWORD_NOT_MATCH =
      "error.user.old.password.not.match";
  public static final String ERROR_USER_UNDERAGE = "error.user.underage";
  public static final String ERROR_ORDER_NOT_FOUND = "error.order.not.found";
  public static final String ERROR_ORDER_STATUS_NOT_FOUND = "error.order.status.not.found";
  public static final String ERROR_ORDER_STATUS_NOT_PENDING = "error.order.status.not.pending";
  public static final String ERROR_USER_INSUFFICIENT_FUNDS = "error.user.insufficient.funds";
}
