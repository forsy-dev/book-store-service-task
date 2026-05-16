package com.forsy.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility class containing centralized constants and helper methods for the web layer.
 *
 * <p>This class serves as the single source of truth for URL routes, view names,
 * path segments, model attributes, and request parameters. It also provide
 * static utility methods for path manipulation, URI expansion, and localized
 * redirection logic.
 *
 * @author Illia
 */
public final class WebConstants {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private WebConstants() {
    throw new UnsupportedOperationException(
        "This is a utility class and cannot be instantiated");
  }

  // --- View Names ---
  public static final String VIEW_BOOKS = "books";
  public static final String VIEW_BOOK_FORM = "book-form";
  public static final String VIEW_BOOK_DETAIL = "book-detail";
  public static final String VIEW_CLIENTS = "clients";
  public static final String VIEW_CLIENT_DETAIL = "client-detail";
  public static final String VIEW_REGISTER_FORM = "register-form";
  public static final String VIEW_ORDERS = "orders";
  public static final String VIEW_PROFILE = "profile";
  public static final String VIEW_CART = "cart";
  public static final String VIEW_LOGIN = "login";
  public static final String VIEW_ERROR = "error";

  // --- Path Segments ---
  public static final String PATH_VAR_NAME = "/{name}";
  public static final String PATH_NEW = "/new";
  public static final String PATH_EDIT = "/edit";
  public static final String PATH_VAR_EMAIL = "/{email}";
  public static final String PATH_PROFILE = "/profile";
  public static final String PATH_BLOCK = "/block";
  public static final String PATH_UNBLOCK = "/unblock";
  public static final String PATH_ADD_BALANCE = "/add-balance";

  // --- URL Routes ---
  public static final String URL_BOOKS = "/books";
  public static final String URL_BOOK_DETAIL = URL_BOOKS + PATH_VAR_NAME;
  public static final String URL_BOOK_NEW = URL_BOOKS + PATH_NEW;
  public static final String URL_BOOK_DETAIL_EDIT = URL_BOOK_DETAIL + PATH_EDIT;
  public static final String URL_ORDERS = "/orders";
  public static final String URL_CLIENTS = "/clients";
  public static final String URL_CLIENT_DETAIL = URL_CLIENTS + PATH_VAR_EMAIL;
  public static final String URL_CLIENT_PROFILE = URL_CLIENTS + PATH_PROFILE;
  public static final String URL_CLIENT_BLOCK = URL_CLIENT_DETAIL + PATH_BLOCK;
  public static final String URL_CLIENT_UNBLOCK = URL_CLIENT_DETAIL + PATH_UNBLOCK;
  public static final String URL_CLIENT_ADD_BALANCE = URL_CLIENT_DETAIL + PATH_ADD_BALANCE;
  public static final String URL_PROFILE = "/profile";
  public static final String URL_LOGIN = "/login";
  public static final String URL_CART = "/cart";

  // --- Attribute Names ---
  public static final String ATTR_BOOK_PAGE = "bookPage";
  public static final String ATTR_KEYWORD = "keyword";
  public static final String ATTR_BOOK = "book";
  public static final String ATTR_ADD_TO_CART_DTO = "addToCartDTO";
  public static final String ATTR_IS_EDIT = "isEdit";
  public static final String ATTR_CLIENT_PAGE = "clientPage";
  public static final String ATTR_CLIENT = "client";
  public static final String ATTR_CLIENT_UPDATE_DTO = "clientUpdateDTO";
  public static final String ATTR_EMPLOYEE_UPDATE_DTO = "employeeUpdateDTO";
  public static final String ATTR_ADD_BALANCE_DTO = "addBalanceDTO";
  public static final String ATTR_ERROR_MESSAGE = "errorMessage";
  public static final String ATTR_SUCCESS_MESSAGE = "successMessage";
  public static final String ATTR_CHANGE_PASSWORD_DTO = "changePasswordDTO";
  public static final String ATTR_USER_PROFILE = "userProfile";
  public static final String ATTR_ERROR = "error";
  public static final String ATTR_CART_ITEMS = "cartItems";
  public static final String ATTR_TOTAL_PRICE_USD = "totalPriceUsd";
  public static final String ATTR_TOTAL_PRICE_UAH = "totalPriceUah";
  public static final String ATTR_STATUS_CODE = "statusCode";
  public static final String ATTR_STATUS_REASON = "statusReason";
  public static final String ATTR_ORDER_PAGE = "orderPage";
  public static final String ATTR_BASE_SEARCH_URL = "baseSearchUrl";
  public static final String ATTR_PAGE_TITLE = "pageTitle";

  // --- Parameter Names ---
  public static final String PARAM_KEYWORD = "keyword";
  public static final String PARAM_ACCOUNT_DELETED = "accountDeleted";

  /**
   * Prefixes a path with the Spring MVC redirect prefix.
   *
   * @param path the destination path
   * @return the formatted redirect string
   */
  public static String redirect(String path) {
    return "redirect:" + path;
  }

  /**
   * Generates the internal key used by Spring's BindingResult to store
   * validation errors for a specific attribute.
   *
   * @param attribute the name of the model attribute
   * @return the full key for the BindingResult
   */
  public static String getBindingResultKey(String attribute) {
    return "org.springframework.validation.BindingResult." + attribute;
  }

  /**
   * Appends URL-encoded query parameters to a given path.
   *
   * @param path       the base URL path
   * @param parameters a map of key-value pairs to append as query parameters
   * @return the complete URL string with encoded parameters
   */
  public static String addParameters(String path, Map<String, String> parameters) {
    if (parameters.isEmpty()) {
      return path;
    }

    StringBuilder builder = new StringBuilder(path);
    if (!path.contains("?")) {
      builder.append("?");
    } else if (!path.endsWith("?") && !path.endsWith("&")) {
      builder.append("&");
    }

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      String encodedKey = URLEncoder.encode(
          entry.getKey(), StandardCharsets.UTF_8);
      String encodedValue = URLEncoder.encode(
          entry.getValue(), StandardCharsets.UTF_8);

      builder.append(encodedKey).append("=").append(encodedValue).append("&");
    }

    builder.deleteCharAt(builder.length() - 1);
    return builder.toString();
  }

  /**
   * Expands template variables within a path using Spring's URI component builder.
   *
   * @param path      the path containing placeholders (e.g., "/{name}")
   * @param variables the values to inject into the placeholders
   * @return the expanded URI string
   */
  public static String expandPathVariables(String path, Object... variables) {
    return UriComponentsBuilder.fromPath(path)
        .buildAndExpand(variables)
        .toUriString();
  }
}
