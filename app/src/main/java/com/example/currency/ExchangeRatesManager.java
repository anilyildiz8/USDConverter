package com.example.currency;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.Map;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;
import java.io.IOException;


public class ExchangeRatesManager {
    private static final String BASE_URL = "https://openexchangerates.org/api/";

    private static final String API_KEY = "cf255cc2bfbf494a854383afb0f365fd";
    private ExchangeRatesResponse latestRates;
    public interface CurrencyNamesService {
        @GET("currency-names")
        Call<Map<String, String>> getCurrencyNames(@Query("currencyCode") String currencyCode);
    }

    public void getCurrencyName(String currencyCode, Callback<Map<String, String>> callback) {
        // Use the CurrencyNamesService interface to create a service instance
        CurrencyNamesService service = RetrofitClient.getRetrofitInstance().create(CurrencyNamesService.class);

        // Make the API call to fetch currency name
        Call<Map<String, String>> call = service.getCurrencyNames(currencyCode);
        call.enqueue(callback);
    }

    public void getLatestExchangeRates(Callback<ExchangeRatesResponse> callback) {
        ExchangeRatesService service = RetrofitClient.getRetrofitInstance().create(ExchangeRatesService.class);
        Call<ExchangeRatesResponse> call = service.getLatestRates(API_KEY);
        call.enqueue(callback);
    }

    public ExchangeRatesResponse getLatestRates() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ExchangeRatesService service = retrofit.create(ExchangeRatesService.class);
        Call<ExchangeRatesResponse> call = service.getLatestRates(API_KEY);

        try {
            Response<ExchangeRatesResponse> response = call.execute();
            if (response.isSuccessful()) {
                latestRates = response.body();
            } else {
                // Handle unsuccessful response
                // You might want to log or throw an exception here
            }
        } catch (IOException e) {
            // Handle IOException
            // You might want to log or throw an exception here
        }

        return latestRates;
    }


}
