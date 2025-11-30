package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
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

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;
    private final MessageSource messageSource;

    @PutMapping("/profile")
    public String updateEmployeeProfile(@Valid @ModelAttribute(name="employeeUpdateDTO") EmployeeUpdateDTO dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Authentication authentication) {

        log.info("Attempting to update profile for employee: {}", authentication.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while updating employee profile: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.employeeUpdateDTO", bindingResult);
            redirectAttributes.addFlashAttribute("employeeUpdateDTO", dto);
            return "redirect:/profile?error=validation";
        }

        String email = authentication.getName();
        try {
            employeeService.updateEmployeeByEmail(email, dto);
            String message = messageSource.getMessage("profile.update.success.message", new Object[]{}, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);

            log.info("Client profile updated for: {}", email);
        } catch (Exception ex) {
            log.warn("Error updating employee: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/profile";
    }
}
