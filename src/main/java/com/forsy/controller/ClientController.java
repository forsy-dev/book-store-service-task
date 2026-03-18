package com.forsy.controller;

import com.forsy.dto.AddBalanceDTO;
import com.forsy.dto.ClientDisplayDTO;
import com.forsy.dto.ClientUpdateDTO;
import com.forsy.service.ClientService;
import com.forsy.util.CartCookieUtil;
import com.forsy.util.MessageKeys;
import com.forsy.util.WebConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Controller
@RequestMapping(WebConstants.URL_CLIENTS)
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;
    private final CartCookieUtil cartCookieUtil;
    private final MessageSource messageSource;

    @GetMapping
    public String getAllClients(Model model,
                                @PageableDefault(sort = "name", direction = Sort.Direction.DESC) Pageable pageable,
                                @RequestParam(name = WebConstants.PARAM_KEYWORD, required = false) String keyword) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        Page<ClientDisplayDTO> clientPage = clientService.getAllClients(pageable, keyword);
        model.addAttribute(WebConstants.ATTR_CLIENT_PAGE, clientPage);
        model.addAttribute(WebConstants.ATTR_KEYWORD, keyword);
        return WebConstants.VIEW_CLIENTS;
    }

    @GetMapping(WebConstants.PATH_VAR_EMAIL)
    public String getClientByEmail(Model model, @PathVariable(name="email") String email) {
        ClientDisplayDTO client = clientService.getClientByEmail(email);
        model.addAttribute(WebConstants.ATTR_CLIENT, client);
        return WebConstants.VIEW_CLIENT_DETAIL;
    }

    @PutMapping(WebConstants.PATH_PROFILE)
    public String updateClientProfile(@Valid @ModelAttribute(name=WebConstants.ATTR_CLIENT_UPDATE_DTO) ClientUpdateDTO dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Authentication authentication) {

        log.info("Attempting to update profile for client: {}", authentication.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while updating client profile: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute(WebConstants.getBindingResultKey(WebConstants.ATTR_CLIENT_UPDATE_DTO), bindingResult);
            redirectAttributes.addFlashAttribute(WebConstants.ATTR_CLIENT_UPDATE_DTO, dto);
            return WebConstants.redirect(WebConstants.addParameters(WebConstants.URL_PROFILE, Map.of("error", "validation")));
        }

        String email = authentication.getName();
        clientService.updateClientByEmail(email, dto);
        String message = messageSource.getMessage(MessageKeys.PROFILE_UPDATE_SUCCESS_MESSAGE, new Object[]{}, LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute(WebConstants.ATTR_SUCCESS_MESSAGE, message);
        log.info("Client profile updated for: {}", email);
        return WebConstants.redirect(WebConstants.URL_PROFILE);
    }

    @DeleteMapping(WebConstants.PATH_PROFILE)
    public String deleteClientProfile(Authentication authentication,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        log.info("Attempting to delete profile for client: {}", authentication.getName());

        clientService.deleteClientByEmail(authentication.getName());

        log.info("Client profile deleted for: {}", authentication.getName());

        Cookie jwtCookie = new Cookie("access_token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        cartCookieUtil.deleteCartCookie(response);

        return WebConstants.redirect(WebConstants.addParameters(WebConstants.URL_LOGIN, Map.of("accountDeleted", "true")));
    }

    @PutMapping(WebConstants.PATH_VAR_EMAIL + WebConstants.PATH_BLOCK)
    public String blockClient(@PathVariable(name="email") String email) {
        log.info("Attempting to block client: {}", email);

        clientService.blockClient(email);

        log.info("Client {} blocked successfully", email);

        return WebConstants.redirect(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, email));
    }

    @PutMapping(WebConstants.PATH_VAR_EMAIL + WebConstants.PATH_UNBLOCK)
    public String unblockClient(@PathVariable(name="email") String email) {
        log.info("Attempting to unblock client: {}", email);

        clientService.unblockClient(email);

        log.info("Client {} unblock successfully", email);

        return WebConstants.redirect(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, email));
    }

    @PostMapping(WebConstants.PATH_VAR_EMAIL + WebConstants.PATH_ADD_BALANCE)
    public String addBalance(@PathVariable("email") String email,
                             @Valid @ModelAttribute(WebConstants.ATTR_ADD_BALANCE_DTO) AddBalanceDTO dto,
                             BindingResult bindingResult,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        log.info("Employee {} adding balance {} to client {}", authentication.getName(), dto.getAmount(), email);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while adding balance: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute(WebConstants.ATTR_ERROR_MESSAGE, messageSource.getMessage(MessageKeys.CLIENT_BALANCE_ERROR_MESSAGE, new Object[]{}, LocaleContextHolder.getLocale()));
            return WebConstants.redirect(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, email));
        }

        try {
            clientService.addBalanceToClient(email, dto);
            redirectAttributes.addFlashAttribute(WebConstants.ATTR_SUCCESS_MESSAGE, messageSource.getMessage(MessageKeys.CLIENT_BALANCE_SUCCESS_MESSAGE, new Object[]{}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            log.warn("Error adding balance: {}", e.getMessage());
            redirectAttributes.addFlashAttribute(WebConstants.ATTR_ERROR_MESSAGE, e.getMessage());
        }

        return WebConstants.redirect(WebConstants.expandPathVariables(WebConstants.URL_CLIENT_DETAIL, email));
    }
}
