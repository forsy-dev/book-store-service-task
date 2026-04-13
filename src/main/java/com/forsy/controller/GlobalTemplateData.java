package com.forsy.controller;

import com.forsy.util.WebConstants;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to populate the model for all web views.
 *
 * <p>This component ensures that essential global data, such as application
 * routing constants, is automatically injected into the model for every
 * HTTP request. This prevents the need to duplicate model attributes across
 * individual controllers and allows the template engine to build dynamic,
 * refactor-safe links.
 *
 * @author Illia
 */
@ControllerAdvice
public class GlobalTemplateData {

  /**
   * Exposes the {@link WebConstants} class to all templates under the name "Routes".
   *
   * <p>By providing the class reference directly to the model, the view layer
   * can access static routing constants (e.g., URLs, parameter names) without
   * relying on hardcoded strings in the HTML files.
   *
   * @return the {@link Class} object representing the application's web constants
   */
  @ModelAttribute("Routes")
  public Class<WebConstants> routes() {
    return WebConstants.class;
  }
}
