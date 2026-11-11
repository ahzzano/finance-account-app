package com.wsantos.finance_backend.services.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Thrown when a registration attempt uses an email that is already taken, rendered as 409 Conflict.
public class EmailAlreadyRegistered extends ResponseStatusException {
    public EmailAlreadyRegistered() {
        super(HttpStatus.CONFLICT, "Email is already registered.");
    }

}
