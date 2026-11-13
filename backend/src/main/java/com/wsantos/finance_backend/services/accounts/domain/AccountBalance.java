package com.wsantos.finance_backend.services.accounts.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_balances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBalance {
    public AccountBalance(UUID accountId, String currency, BigDecimal amount) {
        this.id = new AccountBalanceId(accountId, currency);
        this.amount = amount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }

    private static final int AMOUNT_SCALE = 4;

    @EmbeddedId
    private AccountBalanceId id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** Updates the stored balance amount. */
    public void setAmount(BigDecimal amount) {
        this.amount = amount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }
}
