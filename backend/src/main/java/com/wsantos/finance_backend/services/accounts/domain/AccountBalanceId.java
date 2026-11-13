package com.wsantos.finance_backend.services.accounts.domain;

import java.io.Serializable;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Composite key for an account's balance in a given currency. */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AccountBalanceId implements Serializable {
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "currency", length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;
}
