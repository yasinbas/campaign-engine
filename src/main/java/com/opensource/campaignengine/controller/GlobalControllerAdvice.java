package com.opensource.campaignengine.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.opensource.campaignengine.domain.exception.ResourceNotFoundException; // Ekle
import org.springframework.web.bind.annotation.ExceptionHandler; // Ekle
import org.springframework.web.servlet.ModelAndView; // Ekle

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    // Bu metot, herhangi bir @Controller tarafından bir view döndürülmeden önce çalışır.
    // Dönüş değerini ("username") otomatik olarak tüm modellere ekler.
    @ModelAttribute("username")
    public String getUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null; // Eğer kullanıcı giriş yapmamışsa null döner.
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorMessage", ex.getMessage()); // Hata mesajını view'a gönder
        modelAndView.setViewName("error-404"); // error-404.html sayfasını göster
        return modelAndView;
    }

}