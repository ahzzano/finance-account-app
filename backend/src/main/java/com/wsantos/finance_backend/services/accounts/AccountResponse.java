package com.wsantos.finance_backend.services.accounts;

import java.util.List;
import java.util.UUID;

import com.wsantos.finance_backend.services.accounts.domain.Account;
import com.wsantos.finance_backend.services.accounts.domain.AccountBalance;

// An account along with its balances across all currencies.
public record AccountResponse(UUID id, String name, List<AccountBalanceResponse> balances) {
    // Builds an AccountResponse from an Account entity and its balances.
    public static AccountResponse from(Account account, List<AccountBalance> balances) {
        List<AccountBalanceResponse> balanceResponses = balances.stream()
            .map(balance -> new AccountBalanceResponse(balance.getId().getCurrency(), balance.getAmount()))
            .toList();

        return new AccountResponse(account.getId(), account.getName(), balanceResponses);
    }
}
