package com.wsantos.finance_backend.services.accounts.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, AccountBalanceId> {
    List<AccountBalance> findByIdAccountId(UUID accountId);
}
