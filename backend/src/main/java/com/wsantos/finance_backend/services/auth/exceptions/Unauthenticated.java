package com.wsantos.finance_backend.services.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class Unauthenticated extends RuntimeException{
    public Unauthenticated() {
        super("Forbidden");
    }
    
}
