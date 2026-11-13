package com.wsantos.finance_backend.services.accounts;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

// Request body for setting an account's balance in a given currency.
public record BalanceRequest(@NotNull @DecimalMin("0") @Digits(integer = 15, fraction = 4) BigDecimal amount) {
}
