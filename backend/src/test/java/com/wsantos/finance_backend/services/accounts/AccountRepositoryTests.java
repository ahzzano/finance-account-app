package com.wsantos.finance_backend.services.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import com.wsantos.finance_backend.IntegrationTests;
import com.wsantos.finance_backend.services.accounts.domain.Account;
import com.wsantos.finance_backend.services.accounts.domain.AccountRepository;
import com.wsantos.finance_backend.services.auth.AuthRepository;

class AccountRepositoryTests extends IntegrationTests {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AuthRepository authRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private UUID registerOwner(String email) {
        assertThat(register(email, "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);
        return authRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
    }

    @Test
    void archivedAccountDoesNotBlockReusingItsName() {
        UUID owner = registerOwner("accounts-archived@gmail.com");
        Account account = accountRepository.save(new Account(owner, "Checking"));
        jdbcTemplate.update("UPDATE accounts SET archived_at = now() WHERE id = ?", account.getId());

        assertThat(accountRepository.existsByOwnerAndNameIgnoreCaseAndArchivedAtIsNull(owner, "Checking")).isFalse();
        assertThat(accountRepository.save(new Account(owner, "Checking"))).isNotNull();
    }

    @Test
    void activeAccountWithSameNameDifferentCaseViolatesUniqueIndex() {
        UUID owner = registerOwner("accounts-duplicate@gmail.com");
        accountRepository.saveAndFlush(new Account(owner, "Checking"));

        assertThatThrownBy(() -> accountRepository.saveAndFlush(new Account(owner, "CHECKING")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
