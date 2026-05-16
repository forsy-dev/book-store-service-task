package com.forsy.controller;

import com.forsy.dto.EmployeeUpdateDto;
import com.forsy.service.EmployeeService;
import com.forsy.util.WebConstants;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for handling employee-specific web requests.
 *
 * <p>Provides endpoints for bookstore employees to manage their own accounts,
 * specifically focusing on profile updates and self-service administration
 * within the secure employee dashboard.
 *
 * @author Illia
 */
@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

  private final EmployeeService employeeService;
  private final MessageSource messageSource;

  /**
   * Updates the authenticated employee's personal profile information.
   *
   * <p>Validates the incoming update request payload. If validation fails or
   * an exception occurs during the database update, the employee is redirected
   * back to their profile form with the appropriate error attributes. On success,
   * a localized confirmation message is attached to the redirect.
   *
   * @param dto                the data transfer object containing updated profile details
   * @param bindingResult      holds the results of the DTO validation
   * @param redirectAttributes used to pass flash messages or errors back to the profile view
   * @param authentication     the current employee's authentication details
   * @return a redirect URL string returning the user to their profile page
   */
  @PutMapping("/profile")
  public String updateEmployeeProfile(@Valid @ModelAttribute(name = "employeeUpdateDTO")
                                        EmployeeUpdateDto dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Authentication authentication) {

    log.info("Attempting to update profile for employee: {}", authentication.getName());

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while updating employee profile: {}",
          bindingResult.getAllErrors());
      redirectAttributes.addFlashAttribute(
          WebConstants.getBindingResultKey(WebConstants.ATTR_EMPLOYEE_UPDATE_DTO), bindingResult);
      redirectAttributes.addFlashAttribute(WebConstants.ATTR_EMPLOYEE_UPDATE_DTO, dto);
      return WebConstants.redirect(
          WebConstants.addParameters(WebConstants.URL_PROFILE, Map.of("error", "validation")));
    }

    String email = authentication.getName();
    try {
      employeeService.updateEmployeeByEmail(email, dto);
      String message = messageSource.getMessage("profile.update.success.message",
          new Object[]{}, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute(WebConstants.ATTR_SUCCESS_MESSAGE, message);

      log.info("Client profile updated for: {}", email);
    } catch (Exception ex) {
      log.warn("Error updating employee: {}", ex.getMessage());
      redirectAttributes.addFlashAttribute(WebConstants.ATTR_ERROR_MESSAGE, ex.getMessage());
    }

    return WebConstants.redirect(WebConstants.URL_PROFILE);
  }
}
