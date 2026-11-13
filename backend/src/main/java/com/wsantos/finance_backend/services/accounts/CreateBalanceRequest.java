package com.wsantos.finance_backend.services.accounts;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Request body for creating a new balance in a given currency for an account.
public record CreateBalanceRequest(@NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull @DecimalMin("0") @Digits(integer = 15, fraction = 4) BigDecimal amount) {
}
