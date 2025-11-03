package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientCreateDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ClientService clientService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/books";
        }

        model.addAttribute("client", new ClientCreateDTO());
        return "register-form";
    }

    @PostMapping("/register")
    public String registerClient(@Valid @ModelAttribute("client") ClientCreateDTO client, BindingResult bindingResult,
                                 Model model, Authentication authentication) {
        log.info("Registering client: {}", client);
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/books";
        }
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while registering client: {}", bindingResult.getAllErrors());
            return "register-form";
        }
        try {
            clientService.addClient(client);
            log.info("Client {} registered successfully", client.getEmail());
            return "redirect:/login";
        } catch (AlreadyExistException ex) {
            log.warn("Attempted to register with existing email: {}", client.getEmail());
            model.addAttribute("errorMessage", ex.getMessage());
            return "register-form";
        }
    }
}
