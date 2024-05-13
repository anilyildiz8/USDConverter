package com.example.currency;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    List<String> currencyCodes = Arrays.asList("TRY", "EUR", "GBP", "JPY", "CAD", "AUD", "CNY");
    private CurrencyAdapter adapter;
    private ExchangeRatesManager exchangeRatesManager;
    private List<Currency> currencyList;
    EditText editTextAmount;
    Button buttonConvert;
    Spinner spinnerFromCurrency, spinnerToCurrency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.RecyclerView);
        currencyList = new ArrayList<>();
        exchangeRatesManager = new ExchangeRatesManager();
        adapter = new CurrencyAdapter(this, currencyList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch exchange rates
        fetchExchangeRates();

        // Set up Spinners and Convert button
        setupSpinnersAndButton();

        // Initialize currency list
        initializeCurrencyList();

        editTextAmount = findViewById(R.id.editTextAmount);

        Log.d("InitialEditTextValue", "EditText Amount Value: " + (editTextAmount != null ? editTextAmount.getText().toString() : "null"));

        editTextAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                // Hide the keyboard
                hideKeyboard();
                return true;
            }
            return false;
        });
        hideSystemBars();

    }

    // Define the hideSystemBars() method outside of onCreate()
    private void hideSystemBars() {
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(
                getWindow(), getWindow().getDecorView()
        );

        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editTextAmount.getWindowToken(), 0);
        }
    }

    // Method to set up Spinners and Convert button
    private void setupSpinnersAndButton() {
        // Initialize Spinners
        spinnerFromCurrency = findViewById(R.id.spinnerFromCurrency);
        spinnerToCurrency = findViewById(R.id.spinnerToCurrency);

        // Create ArrayAdapter for the second spinner using currencyCodes list excluding USD
        List<String> currencyCodesWithoutUSD = new ArrayList<>(currencyCodes);
        currencyCodesWithoutUSD.remove("USD");
        ArrayAdapter<String> spinnerAdapterWithoutUSD = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencyCodesWithoutUSD);
        spinnerAdapterWithoutUSD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToCurrency.setAdapter(spinnerAdapterWithoutUSD);

        // Create ArrayAdapter for the first spinner with USD only
        ArrayAdapter<String> spinnerAdapterUSD = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Collections.singletonList("USD"));
        spinnerAdapterUSD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromCurrency.setAdapter(spinnerAdapterUSD);

        // Initialize Convert button
        buttonConvert = findViewById(R.id.buttonConvert);

        // Set OnClickListener for the Convert button
        buttonConvert.setOnClickListener(v -> convertCurrency());
    }

    // Method to initialize currency list
    private void initializeCurrencyList() {
        for (String currencyCode : currencyCodes) {
            currencyList.add(new Currency(0, "", 0.0, currencyCode));
        }
    }

    private int getIconResourceIdForCurrency(String currencyCode) {
        int iconResourceId;
        // Logic to map currency code to corresponding icon resource ID
        switch (currencyCode) {
            case "AUD":
                iconResourceId = R.drawable.aud;
                break;
            case "CAD":
                iconResourceId = R.drawable.cad;
                break;
            case "CNY":
                iconResourceId = R.drawable.cny;
                break;
            case "EUR":
                iconResourceId = R.drawable.euro;
                break;
            case "GBP":
                iconResourceId = R.drawable.gbp;
                break;
            case "JPY":
                iconResourceId = R.drawable.jpy;
                break;
            case "TRY":
                iconResourceId = R.drawable.turk;
                break;
            default:
                iconResourceId = R.drawable.turk; // Default icon resource ID
                break;
        }
        // Log whether the icon resource ID was successfully loaded
        if (iconResourceId != 0) {
            Log.d("IconLoading", "Loaded icon resource ID for " + currencyCode + ": " + iconResourceId);
        } else {
            Log.e("IconLoading", "Failed to load icon resource ID for " + currencyCode);
        }
        return iconResourceId;
    }



    private void convertCurrency() {

        Log.d("EditTextValue", "EditText Amount Value: " + (editTextAmount != null ? editTextAmount.getText().toString() : "null"));


        String fromCurrency = spinnerFromCurrency.getSelectedItem().toString();
        String toCurrency = spinnerToCurrency.getSelectedItem().toString();
        double amount;

        if (editTextAmount == null || editTextAmount.getText().toString().trim().isEmpty()) {

            Log.d("ConvertCurrency", "Empty or null amount entered");
            Toast.makeText(MainActivity.this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            amount = Double.parseDouble(editTextAmount.getText().toString());
        } catch (NumberFormatException e) {

            Toast.makeText(MainActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }


        fetchExchangeRates(new ExchangeRatesCallback() {
            @Override
            public void onSuccess(ExchangeRatesResponse exchangeRatesResponse) {
                Map<String, Double> exchangeRates = exchangeRatesResponse.getExchangeRates();

                if (exchangeRates != null && exchangeRates.containsKey(fromCurrency) && exchangeRates.containsKey(toCurrency)) {
                    double fromRate = exchangeRates.get(fromCurrency);
                    double toRate = exchangeRates.get(toCurrency);
                    double convertedAmount = (amount / fromRate) * toRate;

                    EditText editTextConvertedAmount = findViewById(R.id.editTextConvertedAmount);

                    if (editTextConvertedAmount != null) {
                        editTextConvertedAmount.setText(String.format(Locale.getDefault(), "%.2f %s", convertedAmount, toCurrency));
                    } else {
                        Log.e("MainActivity", "EditTextConvertedAmount is null");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Exchange rates not available for selected currencies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle failure
                Toast.makeText(MainActivity.this, "Failed to fetch exchange rates: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }

    interface ExchangeRatesCallback {
        void onSuccess(ExchangeRatesResponse exchangeRatesResponse);
        void onFailure(String errorMessage);
    }

    // Method to fetch exchange rates asynchronously
    private void fetchExchangeRates(ExchangeRatesCallback callback) {
        exchangeRatesManager.getLatestExchangeRates(new Callback<ExchangeRatesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExchangeRatesResponse> call, @NonNull Response<ExchangeRatesResponse> response) {
                if (response.isSuccessful()) {
                    ExchangeRatesResponse ratesResponse = response.body();
                    if (ratesResponse != null) {
                        Log.d("APIResponse", "Response Body: " + ratesResponse);
                        Log.d("APIResponse", "Base Currency: " + ratesResponse.getBaseCurrency());
                        Log.d("APIResponse", "Exchange Rates: " + ratesResponse.getExchangeRates());
                        callback.onSuccess(ratesResponse);
                    } else {
                        callback.onFailure("Failed to parse exchange rates response.");
                    }
                } else {
                    callback.onFailure("Failed to fetch exchange rates. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ExchangeRatesResponse> call, @NonNull Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    private void fetchExchangeRates() {
        exchangeRatesManager.getLatestExchangeRates(new Callback<ExchangeRatesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExchangeRatesResponse> call, @NonNull Response<ExchangeRatesResponse> response) {
                if (response.isSuccessful()) {
                    ExchangeRatesResponse ratesResponse = response.body();
                    updateUI(ratesResponse);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ExchangeRatesResponse> call, @NonNull Throwable t) {
                Log.e("FetchRatesError", "Network error: " + t.getMessage());
            }
        });
    }

    private void updateUI(ExchangeRatesResponse ratesResponse) {
        for (String currencyCode : currencyCodes) {
            getCurrencyName(currencyCode, new Callback<Map<String, String>>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> currencyNames = response.body();
                        if (currencyNames != null) {
                            String name = currencyNames.get(currencyCode);
                            double rate = ratesResponse.getExchangeRates().getOrDefault(currencyCode, 0.0);
                            int iconResourceId = getIconResourceIdForCurrency(currencyCode);

                            Log.d("CurrencyResponse", "Currency Code: " + currencyCode + ", Currency Name: " + name + ", Rate: " + rate);

                            // Update the currency name, rate, and icon in the list
                            for (Currency currency : currencyList) {
                                if (currency.getCode().equals(currencyCode)) {
                                    currency.setName(name);
                                    currency.setRate(rate);
                                    currency.setIconResourceId(iconResourceId);
                                    break;
                                }
                            }
                            adapter.notifyDataSetChanged(); // Notify adapter of data change
                        } else {
                            Log.e("CurrencyResponse", "Response body is null");
                        }
                    } else {
                        Log.e("CurrencyResponse", "Response unsuccessful: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                    Log.e("CurrencyResponse", "Failed to fetch currency names: " + t.getMessage());
                }
            });
        }
    }
    // Correct the method signature to accept the correct callback type
    public void getCurrencyName(String currencyCode, Callback<Map<String, String>> callback) {
        // Use the CurrencyNamesService interface to create a service instance
        CurrencyNamesService service = RetrofitClient.getRetrofitInstance().create(CurrencyNamesService.class);
        // Make the API call to fetch currency name
        Call<Map<String, String>> call = service.getCurrencyNames(currencyCode);
        call.enqueue(callback);
    }

}
