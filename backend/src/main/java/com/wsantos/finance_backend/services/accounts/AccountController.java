package com.wsantos.finance_backend.services.accounts;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("")
    public List<AccountResponse> list(@SessionAttribute(name = "userId") UUID ownerId) {
        return this.service.list(ownerId);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@SessionAttribute(name = "userId") UUID ownerId, @RequestBody @Valid AccountRequest request) {
        return this.service.create(ownerId, request);
    }

    @GetMapping("/{id}")
    public AccountResponse get(@SessionAttribute(name = "userId") UUID ownerId, @PathVariable UUID id) {
        return this.service.get(ownerId, id);
    }

    @PatchMapping("/{id}")
    public AccountResponse rename(@SessionAttribute(name = "userId") UUID ownerId, @PathVariable UUID id, @RequestBody @Valid AccountRequest request) {
        return this.service.rename(ownerId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@SessionAttribute(name = "userId") UUID ownerId, @PathVariable UUID id) {
        this.service.delete(ownerId, id);
    }

    @PutMapping("/{id}/balances/{currency}")
    public AccountResponse setBalance(@SessionAttribute(name = "userId") UUID ownerId, @PathVariable UUID id, @PathVariable String currency, @RequestBody @Valid BalanceRequest request) {
        return this.service.setBalance(ownerId, id, currency, request);
    }

    @DeleteMapping("/{id}/balances/{currency}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBalance(@SessionAttribute(name = "userId") UUID ownerId, @PathVariable UUID id, @PathVariable String currency) {
        this.service.removeBalance(ownerId, id, currency);
    }
}
