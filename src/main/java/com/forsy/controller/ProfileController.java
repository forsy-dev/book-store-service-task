package com.forsy.controller;

import com.forsy.dto.ChangePasswordDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.ClientUpdateDto;
import com.forsy.dto.EmployeeDisplayDto;
import com.forsy.dto.EmployeeUpdateDto;
import com.forsy.exception.InvalidPasswordException;
import com.forsy.service.ClientService;
import com.forsy.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for managing user profiles and security credentials.
 *
 * <p>Provides a unified interface for both clients and employees to view their
 * account details, initialize update forms with current data, and securely
 * change their passwords. It acts as a polymorphic hub, routing service calls
 * based on the authenticated user's granted authorities.
 *
 * @author Illia
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

  private final ClientService clientService;
  private final EmployeeService employeeService;
  private final ModelMapper mapper;
  private final MessageSource messageSource;

  /**
   * Renders the profile page for the currently authenticated user.
   *
   * <p>Detects the user's role and populates the model with the appropriate
   * display and update DTOs. If validation errors were passed via flash attributes,
   * it preserves the user's input instead of reloading from the database.
   *
   * @param model the Spring MVC model to populate with profile data
   * @param auth  the current user's authentication and role details
   * @return the profile view name
   */
  @GetMapping
  public String showProfilePage(Model model, Authentication auth) {
    String email = auth.getName();

    if (!model.containsAttribute("changePasswordDTO")) {
      model.addAttribute("changePasswordDTO", new ChangePasswordDto());
    }

    if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
      ClientDisplayDto client = clientService.getClientByEmail(email);
      model.addAttribute("userProfile", client);

      if (!model.containsAttribute("clientUpdateDTO")) {
        model.addAttribute("clientUpdateDTO", mapper.map(client, ClientUpdateDto.class));
      }
      model.addAttribute("employeeUpdateDTO", new EmployeeUpdateDto());

    } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))) {
      EmployeeDisplayDto employee = employeeService.getEmployeeByEmail(email);
      model.addAttribute("userProfile", employee);

      if (!model.containsAttribute("employeeUpdateDTO")) {
        model.addAttribute("employeeUpdateDTO", mapper.map(employee, EmployeeUpdateDto.class));
      }

      model.addAttribute("clientUpdateDTO", new ClientUpdateDto());
    }

    return "profile";
  }

  /**
   * Processes the request to update the user's password.
   *
   * <p>Performs validation on the password change request. If valid, identifies
   * the user's role to call the correct service implementation. Handles
   * {@link InvalidPasswordException} by returning the user to the profile page
   * with a localized error message.
   *
   * @param auth               the current user's authentication details
   * @param changePasswordDto  the DTO containing old and new password information
   * @param bindingResult      holds the results of the DTO validation
   * @param redirectAttributes used to pass success or error messages to the next page
   * @return a redirect URL string to the profile page
   */
  @PutMapping("/password")
  String updatePassword(Authentication auth,
                        @Valid @ModelAttribute("changePasswordDTO")
                        ChangePasswordDto changePasswordDto,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
    log.info("Attempting to update password for user: {}", auth.getName());

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while updating password: {}", bindingResult.getAllErrors());

      redirectAttributes.addFlashAttribute(
          "org.springframework.validation.BindingResult.changePasswordDTO", bindingResult);
      redirectAttributes.addFlashAttribute("changePasswordDTO", changePasswordDto);
      return "redirect:/profile?error=validation";
    }

    try {
      if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
        clientService.changePassword(auth.getName(), changePasswordDto);
      } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))) {
        employeeService.changePassword(auth.getName(), changePasswordDto);
      }
      log.info("Password for user with email {} changed successfully", auth.getName());

      String message = messageSource.getMessage("user.password.success.message",
          new Object[]{}, LocaleContextHolder.getLocale());
      redirectAttributes.addFlashAttribute("successMessage", message);
      return "redirect:/profile";
    } catch (InvalidPasswordException ex) {
      log.warn("Invalid old password for user {}: {}", auth.getName(), ex.getMessage());

      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
      redirectAttributes.addFlashAttribute("changePasswordDTO", new ChangePasswordDto());
      return "redirect:/profile?error=service";
    }
  }
}
