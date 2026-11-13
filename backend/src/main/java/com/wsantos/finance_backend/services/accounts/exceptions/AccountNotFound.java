package com.wsantos.finance_backend.services.accounts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Thrown when the requested account does not exist, rendered as 404 Not Found.
public class AccountNotFound extends ResponseStatusException {
    public AccountNotFound() {
        super(HttpStatus.NOT_FOUND, "Account not found.");
    }
}
