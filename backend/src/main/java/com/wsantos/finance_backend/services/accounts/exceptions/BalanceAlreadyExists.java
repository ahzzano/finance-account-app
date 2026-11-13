package com.wsantos.finance_backend.services.accounts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Thrown when creating a balance for a currency that already exists on the account, rendered as 409 Conflict.
public class BalanceAlreadyExists extends ResponseStatusException {
    public BalanceAlreadyExists() {
        super(HttpStatus.CONFLICT, "Balance already exists for this currency.");
    }
}
