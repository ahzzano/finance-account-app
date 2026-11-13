package com.wsantos.finance_backend.services.accounts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Thrown when the requested currency code is not recognized, rendered as 404 Not Found.
public class CurrencyNotFound extends ResponseStatusException {
    public CurrencyNotFound() {
        super(HttpStatus.NOT_FOUND, "Unknown currency.");
    }
}
