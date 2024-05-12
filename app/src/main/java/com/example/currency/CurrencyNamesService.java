package com.example.currency;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.Map;

public interface CurrencyNamesService {
    @GET("/currencies.json")
    Call<Map<String, String>> getCurrencyNames(@Query("currencyCode") String currencyCode);
}

