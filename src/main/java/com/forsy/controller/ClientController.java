package com.forsy.controller;

import com.forsy.dto.AddBalanceDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.ClientUpdateDto;
import com.forsy.service.ClientService;
import com.forsy.util.CartCookieUtil;
import com.forsy.util.MessageKeys;
import com.forsy.util.WebConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for client management and profile operations.
 *
 * <p>Handles both administrative endpoints (viewing all clients, blocking/unblocking,
 * adding balance) typically accessed by employees, and self-service endpoints
 * (updating or deleting profiles) accessed directly by authenticated clients.
 *
 * @author Illia
 */
@Controller
@RequestMapping(WebConstants.URL_CLIENTS)
@RequiredArgsConstructor
@Slf4j
public class ClientController {

  private final ClientService clientService;
  private final CartCookieUtil cartCookieUtil;
  private final MessageSource messageSource;

  /**
   * Retrieves a paginated list of all clients, optionally filtered by a search keyword.
   *
   * @param model    the Spring MVC model to populate with client data
   * @param pageable the pagination and sorting details (defaults to descending by name)
   * @param keyword  an optional search string to filter clients
   * @return the name of the view rendering the client directory
   */
  @GetMapping
  public String getAllClients(Model model,
                              @PageableDefault(sort = "name", direction = Sort.Direction.DESC)
                              Pageable pageable,
                              @RequestParam(name = WebConstants.PARAM_KEYWORD, required = false)
                                String keyword) {
    if (keyword != null && keyword.trim().isEmpty()) {
      keyword = null;
    }
    Page<ClientDisplayDto> clientPage = clientService.getAllClients(pageable, keyword);
    model.addAttribute(WebConstants.ATTR_CLIENT_PAGE, clientPage);
    model.addAttribute(WebConstants.ATTR_KEYWORD, keyword);
    return WebConstants.VIEW_CLIENTS;
  }

  /**
   * Retrieves detailed information for a specific client using their email address.
   *
   * @param model the Spring MVC model to populate with the client's details
   * @param email the unique email address of the client
   * @return the name of the view rendering the client's detailed profile
   */
  @GetMapping(WebConstants.PATH_VAR_EMAIL)
  public String getClientByEmail(Model model, @PathVariable(name = "email") String email) {
    ClientDisplayDto client = clientService.getClientByEmail(email);
    model.addAttribute(WebConstants.ATTR_CLIENT, client);
    return WebConstants.VIEW_CLIENT_DETAIL;
  }

  /**
   * Updates the authenticated client's personal profile information.
   *
   * <p>Validates the incoming update request. If validation fails, redirects the user
   * back to their profile form with error messages. On success, updates the database
   * and displays a localized success message.
   *
   * @param dto                the data transfer object containing updated profile details
   * @param bindingResult      holds the results of the DTO validation
   * @param redirectAttributes used to pass flash messages back to the profile view
   * @param authentication     the current client's authentication details
   * @return a redirect URL string returning the user to their profile page
   */
  @PutMapping(WebConstants.PATH_PROFILE)
  public String updateClientProfile(@Valid @ModelAttribute(
      name = WebConstants.ATTR_CLIENT_UPDATE_DTO) ClientUpdateDto dto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {

    log.info("Attempting to update profile for client: {}", authentication.getName());

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while updating client profile: {}", bindingResult.getAllErrors());
      redirectAttributes.addFlashAttribute(WebConstants.getBindingResultKey(
          WebConstants.ATTR_CLIENT_UPDATE_DTO), bindingResult);
      redirectAttributes.addFlashAttribute(WebConstants.ATTR_CLIENT_UPDATE_DTO, dto);
      return WebConstants.redirect(WebConstants.addParameters(WebConstants.URL_PROFILE,
          Map.of("error", "validation")));
    }

    String email = authentication.getName();
    clientService.updateClientByEmail(email, dto);
    String message = messageSource.getMessage(MessageKeys.PROFILE_UPDATE_SUCCESS_MESSAGE,
        new Object[]{}, LocaleContextHolder.getLocale());
    redirectAttributes.addFlashAttribute(WebConstants.ATTR_SUCCESS_MESSAGE, message);
    log.info("Client profile updated for: {}", email);
    return WebConstants.redirect(WebConstants.URL_PROFILE);
  }

  /**
   * Deletes the authenticated client's account and destroys their session data.
   *
   * <p>Removes the client from the database, then invalidates both their JWT
   * authentication cookie and their shopping cart cookie to ensure a complete and
   * secure logout before redirecting to the login screen.
   *
   * @param authentication the current client's authentication details
   * @param request        the current HTTP request, used for cookie management
   * @param response       the HTTP response, used to overwrite and destroy cookies
   * @return a redirect URL string sending the user to the login page
   */
  @DeleteMapping(WebConstants.PATH_PROFILE)
  public String deleteClientProfile(Authentication authentication,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
    log.info("Attempting to delete profile for client: {}", authentication.getName());

    clientService.deleteClientByEmail(authentication.getName());

    log.info("Client profile deleted for: {}", authentication.getName());

    Cookie jwtCookie = new Cookie("access_token", null);
    jwtCookie.setHttpOnly(true);
    jwtCookie.setSecure(true);
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(0);
    response.addCookie(jwtCookie);

    cartCookieUtil.deleteCartCookie(response);

    return WebConstants.redirect(WebConstants.addParameters(WebConstants.URL_LOGIN,
        Map.of("accountDeleted", "true")));
  }

  /**
   * Blocks a specific client, preventing them from authenticating or using the system.
   *
   * @param email the email address of the client to block
   * @return a redirect URL string returning to the client's detail page
   */
  @PutMapping(WebConstants.PATH_VAR_EMAIL + WebConstants.PATH_BLOCK)
  public String blockClient(@PathVariable(name = "email") String email) {
    log.info("Attempting to block client: {}", email);

    clientService.blockClient(email);

    log.info("Client {} blocked successfully", email);

    return WebConstants.redirect(WebConstants.expandPathVariables(
        WebConstants.URL_CLIENT_DETAIL, email));
  }

  /**
   * Restores system access for a previously blocked client.
   *
   * @param email the email address of the client to unblock
   * @return a redirect URL string returning to the client's detail page
   */
  @PutMapping(WebConstants.PATH_VAR_EMAIL + WebConstants.PATH_UNBLOCK)
  public String unblockClient(@PathVariable(name = "email") String email) {
    log.info("Attempting to unblock client: {}", email);

    clientService.unblockClient(email);

    log.info("Client {} unblock successfully", email);

    return WebConstants.redirect(WebConstants.expandPathVariables(
        WebConstants.URL_CLIENT_DETAIL, email));
  }

  /**
   * Adds funds to a specific client's account balance.
   *
   * <p>Validates the requested amount and attempts to update the database.
   * Localized success or error messages are attached to the redirect based on the outcome.
   *
   * @param email              the email address of the client receiving the funds
   * @param dto                the data transfer object containing the amount to add
   * @param bindingResult      holds the results of the DTO validation
   * @param authentication     the current employee's authentication details (for logging)
   * @param redirectAttributes used to pass success or error messages back to the view
   * @return a redirect URL string returning to the client's detail page
   */
  @PostMapping(WebConstants.PATH_VAR_EMAIL + WebConstants.PATH_ADD_BALANCE)
  public String addBalance(@PathVariable("email") String email,
                           @Valid @ModelAttribute(WebConstants.ATTR_ADD_BALANCE_DTO)
                           AddBalanceDto dto,
                           BindingResult bindingResult,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

    log.info("Employee {} adding balance {} to client {}", authentication.getName(),
        dto.getAmount(), email);

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while adding balance: {}", bindingResult.getAllErrors());
      redirectAttributes.addFlashAttribute(WebConstants.ATTR_ERROR_MESSAGE,
          messageSource.getMessage(MessageKeys.CLIENT_BALANCE_ERROR_MESSAGE,
              new Object[]{}, LocaleContextHolder.getLocale()));
      return WebConstants.redirect(WebConstants.expandPathVariables(
          WebConstants.URL_CLIENT_DETAIL, email));
    }

    try {
      clientService.addBalanceToClient(email, dto);
      redirectAttributes.addFlashAttribute(WebConstants.ATTR_SUCCESS_MESSAGE,
          messageSource.getMessage(MessageKeys.CLIENT_BALANCE_SUCCESS_MESSAGE,
              new Object[]{}, LocaleContextHolder.getLocale()));
    } catch (Exception e) {
      log.warn("Error adding balance: {}", e.getMessage());
      redirectAttributes.addFlashAttribute(WebConstants.ATTR_ERROR_MESSAGE, e.getMessage());
    }

    return WebConstants.redirect(WebConstants.expandPathVariables(
        WebConstants.URL_CLIENT_DETAIL, email));
  }
}
