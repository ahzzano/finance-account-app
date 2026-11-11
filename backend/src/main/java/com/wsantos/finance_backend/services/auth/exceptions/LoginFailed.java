package com.wsantos.finance_backend.services.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Thrown when login credentials are missing or incorrect, rendered as 401 Unauthorized.
public class LoginFailed extends ResponseStatusException {
    public LoginFailed() {
        super(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

}
