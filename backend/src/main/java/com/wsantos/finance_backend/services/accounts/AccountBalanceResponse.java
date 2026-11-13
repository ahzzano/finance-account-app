package com.wsantos.finance_backend.services.accounts;

import java.math.BigDecimal;

// A single currency balance on an account.
public record AccountBalanceResponse(String currency, BigDecimal amount) {
}
