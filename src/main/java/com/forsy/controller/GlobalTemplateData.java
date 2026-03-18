package com.forsy.controller;

import com.forsy.util.WebConstants;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalTemplateData {

    @ModelAttribute("Routes")
    public Class<WebConstants> routes() {
        return WebConstants.class;
    }
}
