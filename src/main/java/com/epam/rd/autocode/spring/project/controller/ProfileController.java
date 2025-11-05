package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.InvalidPasswordException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final ModelMapper mapper;

    // In ProfileController.java
    @GetMapping
    public String showProfilePage(Model model, Authentication auth) {
        String email = auth.getName();

        if (!model.containsAttribute("changePasswordDTO")) {
            model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            // --- This is for the CLIENT ---
            ClientDisplayDTO client = clientService.getClientByEmail(email);
            model.addAttribute("userProfile", client); // For the details box

            // Add the pre-filled DTO for the CLIENT update form
            if (!model.containsAttribute("clientUpdateDTO")) {
                model.addAttribute("clientUpdateDTO", mapper.map(client, ClientUpdateDTO.class));
            }
            // Add an EMPTY DTO for the EMPLOYEE form (to prevent Thymeleaf errors)
            model.addAttribute("employeeUpdateDTO", new EmployeeUpdateDTO());

        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))) {
            // --- This is for the EMPLOYEE ---
            EmployeeDisplayDTO employee = employeeService.getEmployeeByEmail(email);
            model.addAttribute("userProfile", employee); // For the details box

            // Add the pre-filled DTO for the EMPLOYEE update form
            if (!model.containsAttribute("employeeUpdateDTO")) {
                model.addAttribute("employeeUpdateDTO", mapper.map(employee, EmployeeUpdateDTO.class));
            }

            // Add an EMPTY DTO for the CLIENT form (to prevent Thymeleaf errors)
            model.addAttribute("clientUpdateDTO", new ClientUpdateDTO());
        }

        return "profile"; // Renders your profile.html page
    }

    @PutMapping("/password")
    String updatePassword(Authentication auth,
                          @Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO changePasswordDTO,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        log.info("Attempting to update password for user: {}", auth.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while updating password: {}", bindingResult.getAllErrors());

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordDTO", bindingResult);
            redirectAttributes.addFlashAttribute("changePasswordDTO", changePasswordDTO);
            return "redirect:/profile?error=validation";
        }

        try {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
                clientService.changePassword(auth.getName(), changePasswordDTO);
            } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))) {
                employeeService.changePassword(auth.getName(), changePasswordDTO);
            }
            log.info("Password for user with email {} changed successfully", auth.getName());

            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
            return "redirect:/profile";
        } catch (InvalidPasswordException ex) {
            log.warn("Invalid old password for user {}: {}", auth.getName(), ex.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("changePasswordDTO", new ChangePasswordDTO());
            return "redirect:/profile?error=service";
        }
    }
}
