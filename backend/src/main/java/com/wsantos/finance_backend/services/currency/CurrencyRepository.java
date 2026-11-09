package com.wsantos.finance_backend.services.currency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, String>{
    
}
