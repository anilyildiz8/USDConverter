package com.example.currency;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRatesResponse {
    @SerializedName("rates")
    private Map<String, Double> exchangeRates;

    public Map<String, Double> getExchangeRates() {
        return exchangeRates;
    }
}
