package com.forsy.controller;

import com.forsy.conf.jwt.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for handling user authentication operations.
 *
 * <p>Provides endpoints for user login and logout. It manages the creation
 * and destruction of HTTP-only JWT (JSON Web Token) cookies used for secure,
 * stateless session management across the application.
 *
 * @author Illia
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;

  /**
   * Authenticates a user and generates a JWT cookie upon success.
   *
   * <p>Attempts to authenticate the provided credentials using the Spring Security
   * {@link AuthenticationManager}. If successful, a JWT is generated and attached
   * to the response as an HTTP-only cookie, and the user is redirected to the
   * book catalog. If authentication fails, the user is redirected back to the
   * login page with an error flag.
   *
   * @param username           the username submitted from the login form
   * @param password           the password submitted from the login form
   * @param response           the HTTP response to which the JWT cookie is added
   * @param redirectAttributes attributes used to pass error flags upon redirect
   * @return a redirect URL string based on the authentication outcome
   */
  @PostMapping("/login")
  public String login(@RequestParam String username,
                      @RequestParam String password,
                      HttpServletResponse response,
                      RedirectAttributes redirectAttributes) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password)
      );

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String jwt = jwtUtils.generateToken(userDetails);

      Cookie cookie = new Cookie("access_token", jwt);
      cookie.setHttpOnly(true);
      cookie.setPath("/");
      cookie.setMaxAge(24 * 60 * 60); // 1 day
      response.addCookie(cookie);

      return "redirect:/books";

    } catch (AuthenticationException e) {
      log.warn("Login failed for user: {}", username);
      redirectAttributes.addAttribute("error", true);
      return "redirect:/login";
    }
  }

  /**
   * Logs out the current user by destroying their authentication cookie.
   *
   * <p>Overwrites the existing JWT cookie with a null value and a max-age of zero,
   * effectively removing it from the user's browser, and redirects to the login page.
   *
   * @param response the HTTP response to which the expired cookie is added
   * @return a redirect URL string to the login page with a logout flag
   */
  @PostMapping("/logout")
  public String logout(HttpServletResponse response) {
    Cookie cookie = new Cookie("access_token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
    return "redirect:/login?logout";
  }
}
