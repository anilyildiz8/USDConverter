package com.example.currency;

import retrofit2.Call;
import retrofit2.Callback;
public class ExchangeRatesManager {
    private static final String API_KEY = "cf255cc2bfbf494a854383afb0f365fd";
    public void getLatestExchangeRates(Callback<ExchangeRatesResponse> callback) {
        ExchangeRatesService service = RetrofitClient.getRetrofitInstance().create(ExchangeRatesService.class);
        Call<ExchangeRatesResponse> call = service.getLatestRates(API_KEY);
        call.enqueue(callback);
    }

}
