package com.epam.rd.autocode.spring.project.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("Not Found Exception: {} for URL: {}", ex.getMessage(), request.getRequestURL());
        return populateErrorModel(model, ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model, HttpServletRequest request) {
        log.warn("Access Denied Exception: {} for URL: {} by user {}",
                ex.getMessage(),
                request.getRequestURL(),
                request.getRemoteUser()
        );
        model.addAttribute("errorMessage", "You do not have permission to access this resource.");
        return populateErrorModel(model, ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllExceptions(Exception ex, Model model, HttpServletRequest request) {
        log.error("Internal Server Error: {} for URL: {}", ex.getMessage(), request.getRequestURL(), ex);
        return populateErrorModel(model, ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String populateErrorModel(Model model, Exception ex, HttpStatus status) {
        model.addAttribute("statusCode", status.value());
        model.addAttribute("statusReason", status.getReasonPhrase());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("exceptionType", ex.getClass().getSimpleName());
        return "error";
    }
}
