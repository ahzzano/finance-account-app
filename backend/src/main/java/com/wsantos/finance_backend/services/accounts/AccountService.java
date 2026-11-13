package com.wsantos.finance_backend.services.accounts;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wsantos.finance_backend.services.accounts.domain.Account;
import com.wsantos.finance_backend.services.accounts.domain.AccountBalance;
import com.wsantos.finance_backend.services.accounts.domain.AccountBalanceId;
import com.wsantos.finance_backend.services.accounts.domain.AccountBalanceRepository;
import com.wsantos.finance_backend.services.accounts.domain.AccountRepository;
import com.wsantos.finance_backend.services.accounts.exceptions.AccountNameTaken;
import com.wsantos.finance_backend.services.accounts.exceptions.AccountNotFound;
import com.wsantos.finance_backend.services.accounts.exceptions.BalanceAlreadyExists;
import com.wsantos.finance_backend.services.accounts.exceptions.BalanceNotFound;
import com.wsantos.finance_backend.services.accounts.exceptions.CurrencyNotFound;
import com.wsantos.finance_backend.services.currency.CurrencyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final CurrencyRepository currencyRepository;

    public AccountService(
        AccountRepository accountRepository,
        AccountBalanceRepository accountBalanceRepository,
        CurrencyRepository currencyRepository
    ) {
        this.accountRepository = accountRepository;
        this.accountBalanceRepository = accountBalanceRepository;
        this.currencyRepository = currencyRepository;
    }

    // Lists all accounts owned by the user with their balances.
    @Transactional(readOnly = true)
    public List<AccountResponse> list(UUID owner) {
        // shortcut: N+1 balance queries, batch-load when accounts per user grow.
        return this.accountRepository.findByOwnerOrderByName(owner).stream()
            .map(account -> AccountResponse.from(account, this.accountBalanceRepository.findByIdAccountId(account.getId())))
            .toList();
    }

    // Creates a new account for the user with no balances.
    @Transactional
    public AccountResponse create(UUID owner, AccountRequest req) {
        if (this.accountRepository.existsByOwnerAndNameIgnoreCase(owner, req.name())) {
            throw new AccountNameTaken();
        }

        Account saved;
        try {
            saved = this.accountRepository.saveAndFlush(new Account(owner, req.name()));
        } catch (DataIntegrityViolationException ex) {
            throw new AccountNameTaken();
        }

        log.info("Created account {} for owner {}", saved.getId(), owner);
        return AccountResponse.from(saved, List.of());
    }

    // Looks up a single account owned by the user, with its balances.
    @Transactional(readOnly = true)
    public AccountResponse get(UUID owner, UUID id) {
        Account account = this.findOwnedAccount(owner, id);
        return AccountResponse.from(account, this.accountBalanceRepository.findByIdAccountId(account.getId()));
    }

    // Renames an account owned by the user.
    @Transactional
    public AccountResponse rename(UUID owner, UUID id, AccountRequest req) {
        Account account = this.findOwnedAccount(owner, id);
        if (!req.name().equalsIgnoreCase(account.getName())
            && this.accountRepository.existsByOwnerAndNameIgnoreCase(owner, req.name())) {
            throw new AccountNameTaken();
        }

        account.rename(req.name());
        try {
            this.accountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException ex) {
            throw new AccountNameTaken();
        }

        log.info("Renamed account {} for owner {}", id, owner);
        return AccountResponse.from(account, this.accountBalanceRepository.findByIdAccountId(account.getId()));
    }

    // Deletes an account owned by the user; balances cascade via DB FK.
    @Transactional
    public void delete(UUID owner, UUID id) {
        Account account = this.findOwnedAccount(owner, id);
        this.accountRepository.delete(account);
        log.info("Deleted account {} for owner {}", id, owner);
    }

    // Creates a new balance for an account in a currency it does not yet hold.
    @Transactional
    public AccountResponse addBalance(UUID owner, UUID id, CreateBalanceRequest req) {
        String code = req.currency().toUpperCase(Locale.ROOT);
        if (!this.currencyRepository.existsById(code)) {
            throw new CurrencyNotFound();
        }

        Account account = this.findOwnedAccount(owner, id);
        if (this.accountBalanceRepository.findById(new AccountBalanceId(id, code)).isPresent()) {
            throw new BalanceAlreadyExists();
        }

        try {
            this.accountBalanceRepository.saveAndFlush(new AccountBalance(id, code, req.amount()));
        } catch (DataIntegrityViolationException ex) {
            throw new BalanceAlreadyExists();
        }

        log.info("Added balance for account {} currency {}", id, code);
        return AccountResponse.from(account, this.accountBalanceRepository.findByIdAccountId(id));
    }

    // Updates the balance of an account in a currency it already holds.
    @Transactional
    public AccountResponse setBalance(UUID owner, UUID id, String currency, BalanceRequest req) {
        String code = currency.toUpperCase(Locale.ROOT);
        if (!this.currencyRepository.existsById(code)) {
            throw new CurrencyNotFound();
        }

        Account account = this.findOwnedAccount(owner, id);
        AccountBalance balance = this.accountBalanceRepository.findById(new AccountBalanceId(id, code))
            .orElseThrow(BalanceNotFound::new);
        balance.setAmount(req.amount());

        log.info("Set balance for account {} currency {}", id, code);
        return AccountResponse.from(account, this.accountBalanceRepository.findByIdAccountId(id));
    }

    // Removes the balance of an account in the given currency.
    @Transactional
    public void removeBalance(UUID owner, UUID id, String currency) {
        String code = currency.toUpperCase(Locale.ROOT);
        if (!this.currencyRepository.existsById(code)) {
            throw new CurrencyNotFound();
        }

        this.findOwnedAccount(owner, id);
        AccountBalance balance = this.accountBalanceRepository.findById(new AccountBalanceId(id, code))
            .orElseThrow(BalanceNotFound::new);
        this.accountBalanceRepository.delete(balance);
        log.info("Removed balance for account {} currency {}", id, code);
    }

    // Looks up an account scoped to its owner, 404s (never 403) when missing or owned by someone else.
    private Account findOwnedAccount(UUID owner, UUID id) {
        return this.accountRepository.findByIdAndOwner(id, owner)
            .orElseThrow(AccountNotFound::new);
    }
}
