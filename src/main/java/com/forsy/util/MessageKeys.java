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

  // --- Error Message Keys ---

  /**
   * Key for the message returned when a requested book cannot be located.
   */
  public static final String ERROR_BOOK_NOT_FOUND = "error.book.not.found";

  /**
   * Key for the message returned when attempting to add a book that
   * already exists.
   */
  public static final String ERROR_BOOK_ALREADY_EXISTS = "error.book.already.exists";

  // --- Profile Message Keys ---

  /**
   * Key for the success message displayed after a user profile update.
   */
  public static final String PROFILE_UPDATE_SUCCESS_MESSAGE = "profile.update.success.message";

  // --- Balance Message Keys ---

  /**
   * Key for the message returned when a balance-related operation fails.
   */
  public static final String CLIENT_BALANCE_ERROR_MESSAGE = "client.balance.error.message";

  /**
   * Key for the message displayed after a successful balance adjustment.
   */
  public static final String CLIENT_BALANCE_SUCCESS_MESSAGE = "client.balance.success.message";
}
