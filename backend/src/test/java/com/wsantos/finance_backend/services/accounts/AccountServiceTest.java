package com.wsantos.finance_backend.services.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.wsantos.finance_backend.services.accounts.domain.Account;
import com.wsantos.finance_backend.services.accounts.domain.AccountBalance;
import com.wsantos.finance_backend.services.accounts.domain.AccountBalanceId;
import com.wsantos.finance_backend.services.accounts.domain.AccountBalanceRepository;
import com.wsantos.finance_backend.services.accounts.domain.AccountRepository;
import com.wsantos.finance_backend.services.accounts.exceptions.AccountNotFound;
import com.wsantos.finance_backend.services.accounts.exceptions.BalanceNotFound;
import com.wsantos.finance_backend.services.accounts.exceptions.CurrencyNotFound;
import com.wsantos.finance_backend.services.currency.CurrencyRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    private AccountService accountService;

    private UUID owner;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        this.accountService = new AccountService(accountRepository, accountBalanceRepository, currencyRepository);
        this.owner = UUID.randomUUID();
        this.accountId = UUID.randomUUID();
    }

    private Account ownedAccount() {
        Account account = new Account(owner, "Checking");
        ReflectionTestUtils.setField(account, "id", accountId);
        return account;
    }

    @Test
    void createStoresOwnerAndName() {
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        Account saved = ownedAccount();
        when(accountRepository.save(any())).thenReturn(saved);

        AccountResponse response = accountService.create(owner, new AccountRequest("Checking"));

        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isEqualTo(owner);
        assertThat(captor.getValue().getName()).isEqualTo("Checking");
        assertThat(response).isNotNull();
    }

    @Test
    void getThrowsAccountNotFoundWhenNotOwned() {
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.get(owner, accountId))
            .isInstanceOf(AccountNotFound.class);
    }

    @Test
    void renameThrowsAccountNotFoundWhenNotOwned() {
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.rename(owner, accountId, new AccountRequest("New name")))
            .isInstanceOf(AccountNotFound.class);
    }

    @Test
    void deleteThrowsAccountNotFoundWhenNotOwned() {
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.delete(owner, accountId))
            .isInstanceOf(AccountNotFound.class);
        verify(accountRepository, never()).delete(any());
    }

    @Test
    void setBalanceThrowsAccountNotFoundWhenNotOwned() {
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.setBalance(owner, accountId, "USD", new BalanceRequest(BigDecimal.TEN)))
            .isInstanceOf(AccountNotFound.class);
        verify(accountBalanceRepository, never()).save(any());
    }

    @Test
    void removeBalanceThrowsAccountNotFoundWhenNotOwned() {
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.removeBalance(owner, accountId, "USD"))
            .isInstanceOf(AccountNotFound.class);
        verify(accountBalanceRepository, never()).delete(any());
    }

    @Test
    void deleteRemovesTheAccount() {
        Account account = ownedAccount();
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.of(account));

        accountService.delete(owner, accountId);

        verify(accountRepository).delete(account);
    }

    @Test
    void setBalanceCreatesNewBalanceWhenAbsent() {
        Account account = ownedAccount();
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.of(account));
        when(currencyRepository.existsById("USD")).thenReturn(true);
        when(accountBalanceRepository.findById(new AccountBalanceId(accountId, "USD"))).thenReturn(Optional.empty());
        when(accountBalanceRepository.findByIdAccountId(accountId)).thenReturn(List.of());

        accountService.setBalance(owner, accountId, "USD", new BalanceRequest(BigDecimal.valueOf(100)));

        ArgumentCaptor<AccountBalance> captor = ArgumentCaptor.forClass(AccountBalance.class);
        verify(accountBalanceRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void setBalanceUpdatesExistingBalance() {
        Account account = ownedAccount();
        AccountBalance existing = new AccountBalance(accountId, "USD", BigDecimal.ONE);
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.of(account));
        when(currencyRepository.existsById("USD")).thenReturn(true);
        when(accountBalanceRepository.findById(new AccountBalanceId(accountId, "USD"))).thenReturn(Optional.of(existing));
        when(accountBalanceRepository.findByIdAccountId(accountId)).thenReturn(List.of(existing));

        accountService.setBalance(owner, accountId, "USD", new BalanceRequest(BigDecimal.valueOf(50)));

        assertThat(existing.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        verify(accountBalanceRepository, never()).save(any());
    }

    @Test
    void setBalanceNormalizesLowercaseCurrency() {
        Account account = ownedAccount();
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.of(account));
        when(currencyRepository.existsById("USD")).thenReturn(true);
        when(accountBalanceRepository.findById(new AccountBalanceId(accountId, "USD"))).thenReturn(Optional.empty());
        when(accountBalanceRepository.findByIdAccountId(accountId)).thenReturn(List.of());

        accountService.setBalance(owner, accountId, "usd", new BalanceRequest(BigDecimal.TEN));

        verify(currencyRepository).existsById("USD");
    }

    @Test
    void setBalanceThrowsCurrencyNotFoundForUnknownCode() {
        Account account = ownedAccount();
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.of(account));
        when(currencyRepository.existsById("XYZ")).thenReturn(false);

        assertThatThrownBy(() -> accountService.setBalance(owner, accountId, "xyz", new BalanceRequest(BigDecimal.TEN)))
            .isInstanceOf(CurrencyNotFound.class);
    }

    @Test
    void removeBalanceThrowsBalanceNotFoundWhenAbsent() {
        Account account = ownedAccount();
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.of(account));
        when(accountBalanceRepository.findById(new AccountBalanceId(accountId, "USD"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.removeBalance(owner, accountId, "USD"))
            .isInstanceOf(BalanceNotFound.class);
    }

    @Test
    void removeBalanceDeletesWhenPresent() {
        Account account = ownedAccount();
        AccountBalance existing = new AccountBalance(accountId, "USD", BigDecimal.ONE);
        when(accountRepository.findByIdAndOwner(accountId, owner)).thenReturn(Optional.of(account));
        when(accountBalanceRepository.findById(new AccountBalanceId(accountId, "USD"))).thenReturn(Optional.of(existing));

        accountService.removeBalance(owner, accountId, "USD");

        verify(accountBalanceRepository).delete(existing);
    }
}
