package com.example.currency;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRatesResponse {
    @SerializedName("base")
    private String baseCurrency;
    @SerializedName("rates")
    private Map<String, Double> exchangeRates;

    // Constructor to initialize the fields
    public ExchangeRatesResponse(String baseCurrency, Map<String, Double> exchangeRates) {
        this.baseCurrency = baseCurrency;
        this.exchangeRates = exchangeRates;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public Map<String, Double> getExchangeRates() {
        return exchangeRates;
    }
}
