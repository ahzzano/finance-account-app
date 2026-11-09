package com.wsantos.finance_backend.services.currency;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.wsantos.finance_backend.IntegrationTests;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyControllerTests extends IntegrationTests {

    @Test
    void listsSeededCurrencies() {
        assertThat(mvc.get().uri("/api/currencies"))
                .hasStatusOk()
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isLenientlyEqualTo("""
                        [
                          {"code": "USD", "name": "US Dollar"},
                          {"code": "PHP", "name": "Philippine Peso"}
                        ]
                        """);
    }
}
