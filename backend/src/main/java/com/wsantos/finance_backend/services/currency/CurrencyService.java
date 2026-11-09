package com.wsantos.finance_backend.services.currency;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {
    private final CurrencyRepository repository;

    CurrencyService(CurrencyRepository repository) {
        this.repository = repository;
    }

    // Returns all currencies.
    public List<Currency> findAll() {
        return repository.findAll();
    }
}
