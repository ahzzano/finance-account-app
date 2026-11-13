package com.wsantos.finance_backend.services.accounts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Thrown when the requested account has no balance in the given currency, rendered as 404 Not Found.
public class BalanceNotFound extends ResponseStatusException {
    public BalanceNotFound() {
        super(HttpStatus.NOT_FOUND, "Balance not found.");
    }
}
