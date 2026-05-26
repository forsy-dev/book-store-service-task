package com.forsy.controller;

import com.forsy.dto.ClientCreateDto;
import com.forsy.exception.AlreadyExistException;
import com.forsy.service.ClientService;
import com.forsy.util.WebConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller responsible for public-facing navigation and account creation.
 *
 * <p>Handles the primary entry points of the application, including the home redirect,
 * login page rendering, and the multi-step registration process for new clients.
 * It also manages the display of security-related error pages.
 *
 * @author Illia
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

  private final ClientService clientService;

  /**
   * Redirects the root URL to the book catalog.
   *
   * @return a redirect URL string to the books list
   */
  @GetMapping("/")
  public String home() {
    return WebConstants.redirect(WebConstants.URL_BOOKS);
  }

  /**
   * Displays the login page.
   *
   * <p>If the user is already authenticated, they are redirected to the book catalog
   * instead of showing the login form.
   *
   * @param authentication the current user's authentication details
   * @return the login view name or a redirect to the catalog
   */
  @GetMapping("/login")
  public String showLoginPage(Authentication authentication) {
    if (isAuthenticated(authentication)) {
      return WebConstants.redirect(WebConstants.URL_BOOKS);
    }
    return WebConstants.VIEW_LOGIN;
  }

  /**
   * Displays the registration form for new clients.
   *
   * <p>Populates the model with a fresh {@link ClientCreateDto}. Already authenticated
   * users are redirected away from the registration page.
   *
   * @param model          the Spring MVC model to populate with a new client DTO
   * @param authentication the current user's authentication details
   * @return the registration form view name or a redirect to the catalog
   */
  @GetMapping("/register")
  public String showRegisterPage(Model model, Authentication authentication) {
    if (isAuthenticated(authentication)) {
      return WebConstants.redirect(WebConstants.URL_BOOKS);
    }

    model.addAttribute(WebConstants.ATTR_CLIENT, new ClientCreateDto());
    return WebConstants.VIEW_REGISTER_FORM;
  }

  /**
   * Processes the submission of the client registration form.
   *
   * <p>Performs validation on the client data. If validation fails or the email
   * already exists, the user is returned to the form with error messages.
   * Upon successful registration, the user is redirected to the login page.
   *
   * @param client         the data transfer object containing registration details
   * @param bindingResult  holds the results of the DTO validation
   * @param model          the Spring MVC model to attach error messages if registration fails
   * @param authentication the current user's authentication details
   * @return a redirect to the login page on success, or the registration form on failure
   */
  @PostMapping("/register")
  public String registerClient(@Valid @ModelAttribute(WebConstants.ATTR_CLIENT)
      ClientCreateDto client,
      BindingResult bindingResult,
      Model model,
      Authentication authentication) {
    log.info("Registering client: {}", client);
    if (authentication != null && authentication.isAuthenticated()) {
      return WebConstants.redirect(WebConstants.URL_BOOKS);
    }
    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while registering client: {}", bindingResult.getAllErrors());
      return WebConstants.VIEW_REGISTER_FORM;
    }
    try {
      clientService.addClient(client);
      log.info("Client {} registered successfully", client.getEmail());
      return WebConstants.redirect(WebConstants.URL_LOGIN);
    } catch (AlreadyExistException ex) {
      log.warn("Attempted to register with existing email: {}", client.getEmail());
      model.addAttribute(WebConstants.ATTR_ERROR_MESSAGE, ex.getMessage());
      return WebConstants.VIEW_REGISTER_FORM;
    }
  }

  /**
   * Renders a custom error page for unauthorized access attempts (403 Forbidden).
   *
   * @param model the Spring MVC model to populate with error status details
   * @return the generic error view name
   */
  @RequestMapping(value = "/access-denied", method = {RequestMethod.GET, RequestMethod.POST})
  public String accessDenied(Model model) {
    model.addAttribute(WebConstants.ATTR_STATUS_CODE, 403);
    model.addAttribute(WebConstants.ATTR_STATUS_REASON, "Forbidden");
    model.addAttribute(
        WebConstants.ATTR_ERROR_MESSAGE, "You do not have permission to access this resource.");
    return WebConstants.VIEW_ERROR;
  }

  /**
   * Helper method to determine if a user is truly authenticated.
   *
   * <p>Checks that the authentication object exists, is authenticated, and is not
   * a placeholder anonymous token.
   *
   * @param authentication the authentication object to check
   * @return {@code true} if the user is a known, authenticated entity; {@code false} otherwise
   */
  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }
}
