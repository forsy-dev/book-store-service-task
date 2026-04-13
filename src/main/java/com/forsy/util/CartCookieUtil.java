package com.forsy.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility component for managing shopping cart persistence via browser cookies.
 *
 * <p>This utility handles the transformation of cart data (book names and
 * quantities) into a serialized, Base64-encoded string stored in a client-side
 * cookie. It ensures that cart contents persist across user sessions for a
 * specified duration.
 *
 * @author Illia
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartCookieUtil {

  /**
   * The name of the cookie used to store the serialized cart data.
   */
  private static final String CART_COOKIE_NAME = "cart_cookie";

  /**
   * The lifespan of the cart cookie, set to 7 days in seconds.
   */
  private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

  private final ObjectMapper objectMapper;

  /**
   * Extracts and deserializes the shopping cart from the request cookies.
   *
   * @param request the current {@link HttpServletRequest} containing cookies
   * @return a {@link Map} of book names to quantities, or an empty map if
   *     the cookie is missing or invalid
   */
  public Map<String, Integer> getCartFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (CART_COOKIE_NAME.equals(cookie.getName())) {
          return deserializeCart(cookie.getValue());
        }
      }
    }
    return new HashMap<>();
  }

  /**
   * Serializes the provided cart map and saves it as an HTTP-only cookie.
   *
   * <p>The cookie is set with a global path and a 7-day expiration. The
   * {@code HttpOnly} flag is enabled to prevent client-side script access.
   *
   * @param response the current {@link HttpServletResponse} to add the cookie to
   * @param cart     the map representing the current state of the shopping cart
   */
  public void saveCartToCookie(HttpServletResponse response, Map<String, Integer> cart) {
    String value = serializeCart(cart);
    Cookie cookie = new Cookie(CART_COOKIE_NAME, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(COOKIE_MAX_AGE);
    response.addCookie(cookie);
  }

  /**
   * Invalidates the cart cookie by setting its max age to zero.
   *
   * @param response the current {@link HttpServletResponse} to clear the cookie from
   */
  public void deleteCartCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(CART_COOKIE_NAME, "");
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  /**
   * Converts the cart map into a Base64-encoded JSON string.
   *
   * @param cart the cart map to serialize
   * @return a Base64-encoded string representing the cart, or an empty
   *     string if serialization fails
   */
  private String serializeCart(Map<String, Integer> cart) {
    try {
      String json = objectMapper.writeValueAsString(cart);
      return Base64.getEncoder().encodeToString(
          json.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      log.error("Error serializing cart", e);
      return "";
    }
  }

  /**
   * Decodes and parses a Base64-encoded JSON string back into a cart map.
   *
   * @param cookieValue the raw value from the cart cookie
   * @return the reconstructed cart map, or an empty map if
   *     deserialization fails
   */
  private Map<String, Integer> deserializeCart(String cookieValue) {
    try {
      byte[] decodedBytes = Base64.getDecoder().decode(cookieValue);
      String json = new String(decodedBytes, StandardCharsets.UTF_8);
      return objectMapper.readValue(json,
                                    new TypeReference<HashMap<String, Integer>>() {});
    } catch (Exception e) {
      log.error("Error deserializing cart", e);
      return new HashMap<>();
    }
  }
}
