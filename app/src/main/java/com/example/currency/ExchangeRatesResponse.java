package com.example.currency;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRatesResponse {
    @SerializedName("base")
    private String baseCurrency;

    @SerializedName("rates")
    private Map<String, Double> exchangeRates;

    @SerializedName("names")
    private Map<String, String> currencyNames; // Added field to hold currency names

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public Map<String, Double> getExchangeRates() {
        return exchangeRates;
    }

    public Map<String, String> getCurrencyNames() {
        return currencyNames;
    }
}
