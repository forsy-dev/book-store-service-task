package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDisplayDTO;
import com.epam.rd.autocode.spring.project.dto.ClientUpdateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public String getAllClients(Model model, @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<ClientDisplayDTO> clientPage = clientService.getAllClients(pageable);
        model.addAttribute("clientPage", clientPage);
        return "clients";
    }

    @GetMapping("/{email}")
    public String getClientByEmail(Model model, @PathVariable(name="email") String email) {
        ClientDisplayDTO client = clientService.getClientByEmail(email);
        model.addAttribute("client", client);
        return "client-detail";
    }

    @PutMapping("/profile")
    public String updateClientProfile(@Valid @ModelAttribute(name="clientUpdateDTO") ClientUpdateDTO dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Authentication authentication) {

        log.info("Attempting to update profile for client: {}", authentication.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while updating client profile: {}", bindingResult.getAllErrors());
            // We must re-populate the model for the profile page to render
            // This is a complex page, so we must add all required attributes back.
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.clientUpdateDTO", bindingResult);
            redirectAttributes.addFlashAttribute("clientUpdateDTO", dto);
            return "redirect:/profile?error=validation";
        }

        String email = authentication.getName();
        clientService.updateClientByEmail(email, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        log.info("Client profile updated for: {}", email);
        return "redirect:/profile";
    }

    @DeleteMapping("/profile")
    public String deleteClientProfile(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        log.info("Attempting to delete profile for client: {}", authentication.getName());

        clientService.deleteClientByEmail(authentication.getName());

        log.info("Client profile deleted for: {}", authentication.getName());

        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);

        return "redirect:/login?accountDeleted=true";
    }

    @PutMapping("/{email}/block")
    public String blockClient(@PathVariable(name="email") String email) {
        log.info("Attempting to block client: {}", email);

        clientService.blockClient(email);

        log.info("Client {} blocked successfully", email);
        return "redirect:/clients/" + email;
    }

    @PutMapping("/{email}/unblock")
    public String unblockClient(@PathVariable(name="email") String email) {
        log.info("Attempting to unblock client: {}", email);

        clientService.unblockClient(email);

        log.info("Client {} unblock successfully", email);
        return "redirect:/clients/" + email;
    }
}
