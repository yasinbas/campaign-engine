package com.opensource.campaignengine.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Bu hata fırlatıldığında, Spring'in otomatik olarak 404 NOT FOUND durum kodu dönmesini sağlar.
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}