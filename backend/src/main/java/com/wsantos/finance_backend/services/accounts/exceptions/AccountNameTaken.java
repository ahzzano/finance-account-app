package com.wsantos.finance_backend.services.accounts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Thrown when an account name is already used by another account, rendered as 409 Conflict.
public class AccountNameTaken extends ResponseStatusException {
    public AccountNameTaken() {
        super(HttpStatus.CONFLICT, "Account name already in use.");
    }
}
