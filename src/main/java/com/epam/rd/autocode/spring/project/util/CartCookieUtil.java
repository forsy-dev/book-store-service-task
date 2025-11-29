package com.epam.rd.autocode.spring.project.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartCookieUtil {

    private static final String CART_COOKIE_NAME = "cart_cookie";
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days
    private final ObjectMapper objectMapper;

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

    public void saveCartToCookie(HttpServletResponse response, Map<String, Integer> cart) {
        String value = serializeCart(cart);
        Cookie cookie = new Cookie(CART_COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    public void deleteCartCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(CART_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String serializeCart(Map<String, Integer> cart) {
        try {
            String json = objectMapper.writeValueAsString(cart);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error serializing cart", e);
            return "";
        }
    }

    private Map<String, Integer> deserializeCart(String cookieValue) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cookieValue);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, new TypeReference<HashMap<String, Integer>>() {});
        } catch (Exception e) {
            log.error("Error deserializing cart", e);
            return new HashMap<>();
        }
    }
}
