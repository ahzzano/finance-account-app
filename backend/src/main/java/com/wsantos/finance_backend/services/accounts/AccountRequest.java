package com.wsantos.finance_backend.services.accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request body for creating or renaming an account.
public record AccountRequest(@NotBlank @Size(max = 100) String name) {
}
