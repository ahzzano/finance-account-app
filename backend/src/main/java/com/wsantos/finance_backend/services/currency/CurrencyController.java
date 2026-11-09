package com.wsantos.finance_backend.services.currency;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;


@RestController 
public class CurrencyController {
    private final CurrencyRepository repository;

    CurrencyController(CurrencyRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/currencies")
    public List<Currency> getCurrencies() {
        return repository.findAll();
    }
    
    
}
