package com.example.currency;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExchangeRatesService {
    @GET("latest.json")
    Call<ExchangeRatesResponse> getLatestRates(@Query("app_id") String appId);
}
