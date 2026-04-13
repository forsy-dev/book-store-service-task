package com.forsy.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global interceptor for handling exceptions thrown by any controller within the application.
 *
 * <p>This component centralizes the logic for capturing various runtime exceptions,
 * logging the failures for administrative review, and populating a unified error
 * model to provide user-friendly feedback on the frontend.
 *
 * @author Illia
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles exceptions related to invalid client requests or business logic violations.
   *
   * <p>Captures issues such as duplicate registration, insufficient balance, age
   * restrictions, or password mismatches. Returns a 400 Bad Request status.
   *
   * @param ex      the exception caught during request processing
   * @param model   the Spring MVC model to populate with error details
   * @param request the current HTTP request, used for logging the failing URL
   * @return the name of the generic error view
   */
  @ExceptionHandler({
      AlreadyExistException.class,
      InsufficientFundsException.class,
      AgeRestrictionException.class,
      InvalidPasswordException.class,
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleBadRequestExceptions(Exception ex, Model model, HttpServletRequest request) {
    log.warn("Bad Request Exception: {} for URL: {}", ex.getMessage(), request.getRequestURL());
    return populateErrorModel(model, ex, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles cases where a requested resource (e.g., a specific book or client)
   * cannot be found.
   *
   * <p>Returns a 404 Not Found status.
   *
   * @param ex      the NotFoundException instance
   * @param model   the Spring MVC model
   * @param request the current HTTP request
   * @return the name of the generic error view
   */
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNotFoundException(NotFoundException ex, Model model,
                                        HttpServletRequest request) {
    log.warn("Not Found Exception: {} for URL: {}", ex.getMessage(), request.getRequestURL());
    return populateErrorModel(model, ex, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles security violations when a user lacks the necessary authority.
   *
   * <p>Returns a 403 Forbidden status and provides a generic security message
   * to avoid leaking sensitive system details.
   *
   * @param ex      the AccessDeniedException instance
   * @param model   the Spring MVC model
   * @param request the current HTTP request
   * @return the name of the generic error view
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String handleAccessDeniedException(AccessDeniedException ex, Model model,
                                            HttpServletRequest request) {
    log.warn("Access Denied Exception: {} for URL: {} by user {}",
        ex.getMessage(),
        request.getRequestURL(),
        request.getRemoteUser()
    );
    model.addAttribute("errorMessage", "You do not have permission to access this resource.");
    return populateErrorModel(model, ex, HttpStatus.FORBIDDEN);
  }

  /**
   * Catch-all handler for any unexpected server-side errors.
   *
   * <p>Logs the full stack trace for debugging and returns a 500 Internal
   * Server Error status to the user.
   *
   * @param ex      the unhandled exception
   * @param model   the Spring MVC model
   * @param request the current HTTP request
   * @return the name of the generic error view
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleAllExceptions(Exception ex, Model model, HttpServletRequest request) {
    log.error("Internal Server Error: {} for URL: {}",
        ex.getMessage(), request.getRequestURL(), ex);
    return populateErrorModel(model, ex, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Shared logic for populating the model with standardized error information.
   *
   * @param model  the model to be passed to the view
   * @param ex     the exception being handled
   * @param status the HTTP status determined by the handler
   * @return the string "error", corresponding to the error.html template
   */
  private String populateErrorModel(Model model, Exception ex, HttpStatus status) {
    model.addAttribute("statusCode", status.value());
    model.addAttribute("statusReason", status.getReasonPhrase());
    model.addAttribute("errorMessage", ex.getMessage());
    model.addAttribute("exceptionType", ex.getClass().getSimpleName());
    return "error";
  }
}
