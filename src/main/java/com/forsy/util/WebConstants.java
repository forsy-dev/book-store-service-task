package com.forsy.util;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class WebConstants {

    private WebConstants() {}

    // View Names
    // Books
    public static final String VIEW_BOOKS = "books";
    public static final String VIEW_BOOK_FORM = "book-form";
    public static final String VIEW_BOOK_DETAIL = "book-detail";

    // Clients
    public static final String VIEW_CLIENTS = "clients";
    public static final String VIEW_CLIENT_DETAIL = "client-detail";

    // Path Segments
    // Books
    public static final String PATH_VAR_NAME = "/{name}";
    public static final String PATH_NEW = "/new";
    public static final String PATH_EDIT = "/edit";

    // Clients
    public static final String PATH_VAR_EMAIL = "/{email}";
    public static final String PATH_PROFILE = "/profile";
    public static final String PATH_BLOCK = "/block";
    public static final String PATH_UNBLOCK = "/unblock";
    public static final String PATH_ADD_BALANCE = "/add-balance";

    // URL Routes
    // Books
    public static final String URL_BOOKS = "/books";
    public static final String URL_BOOK_DETAIL = URL_BOOKS + PATH_VAR_NAME;

    // Clients
    public static final String URL_CLIENTS = "/clients";
    public static final String URL_CLIENT_DETAIL = URL_CLIENTS + PATH_VAR_EMAIL;
    public static final String URL_CLIENT_PROFILE = URL_CLIENTS + PATH_PROFILE;
    public static final String URL_CLIENT_BLOCK = URL_CLIENT_DETAIL + PATH_BLOCK;
    public static final String URL_CLIENT_UNBLOCK = URL_CLIENT_DETAIL + PATH_UNBLOCK;
    public static final String URL_CLIENT_ADD_BALANCE = URL_CLIENT_DETAIL + PATH_ADD_BALANCE;

    // Common
    public static final String URL_PROFILE = "/profile";
    public static final String URL_LOGIN = "/login";

    // Attributes Names
    // Books
    public static final String ATTR_BOOK_PAGE = "bookPage";
    public static final String ATTR_KEYWORD = "keyword";
    public static final String ATTR_BOOK = "book";
    public static final String ATTR_ADD_TO_CART_DTO = "addToCartDTO";
    public static final String ATTR_IS_EDIT = "isEdit";

    // Clients
    public static final String ATTR_CLIENT_PAGE = "clientPage";
    public static final String ATTR_CLIENT = "client";
    public static final String ATTR_CLIENT_UPDATE_DTO = "clientUpdateDTO";
    public static final String ATTR_ADD_BALANCE_DTO = "addBalanceDTO";

    // Error
    public static final String ATTR_ERROR_MESSAGE = "errorMessage";

    // Success
    public static final String ATTR_SUCCESS_MESSAGE = "successMessage";

    // Parameters Names
    public static final String PARAM_KEYWORD = "keyword";
    public static final String PARAM_ACCOUNT_DELETED = "accountDeleted";

    public static String redirect(String path) {
        return "redirect:" + path;
    }

    public static String getBindingResultKey(String attribute) {
        return "org.springframework.validation.BindingResult." + attribute;
    }

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
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);

            builder.append(encodedKey).append("=").append(encodedValue).append("&");
        }

        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static String expandPathVariables(String path, Object... variables) {
        return UriComponentsBuilder.fromPath(path).buildAndExpand(variables).toUriString();
    }
}
