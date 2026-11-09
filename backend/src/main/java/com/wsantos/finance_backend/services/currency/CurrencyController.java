package com.wsantos.finance_backend.services.currency;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;


@RestController 
public class CurrencyController {
    private final CurrencyService service;

    CurrencyController(CurrencyService service) {
        this.service = service;
    }

    @GetMapping("/api/currencies")
    public List<Currency> getCurrencies() {
        return service.findAll();
    }
    
    
}
