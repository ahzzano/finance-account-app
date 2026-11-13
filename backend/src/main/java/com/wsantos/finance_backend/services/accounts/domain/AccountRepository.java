package com.wsantos.finance_backend.services.accounts.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByOwnerOrderByName(UUID owner);

    Optional<Account> findByIdAndOwner(UUID id, UUID owner);

    boolean existsByOwnerAndNameIgnoreCase(UUID owner, String name);
}
