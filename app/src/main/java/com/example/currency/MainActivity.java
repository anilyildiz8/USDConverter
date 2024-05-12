package com.example.currency;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import java.util.Locale;
import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {

    List<String> currencyCodes = Arrays.asList("TRY", "EUR", "GBP", "JPY", "CAD", "AUD", "CNY");
    private RecyclerView recyclerView;
    private CurrencyAdapter adapter;
    private ExchangeRatesManager exchangeRatesManager;
    private List<Currency> currencyList;
    EditText editTextAmount;
    Button buttonConvert;
    Spinner spinnerFromCurrency, spinnerToCurrency;
    ArrayAdapter<String> spinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.RecyclerView);
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

        editTextAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Hide the keyboard
                    hideKeyboard();
                    return true;
                }
                return false;
            }
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
        ArrayAdapter<String> spinnerAdapterUSD = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("USD"));
        spinnerAdapterUSD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromCurrency.setAdapter(spinnerAdapterUSD);

        // Initialize Convert button
        buttonConvert = findViewById(R.id.buttonConvert);

        // Set OnClickListener for the Convert button
        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });
    }

    // Method to initialize currency list
    private void initializeCurrencyList() {
        for (String currencyCode : currencyCodes) {
            int iconResId = getIconResourceIdForCurrency(currencyCode);
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
        // Log the current value of editTextAmount
        Log.d("EditTextValue", "EditText Amount Value: " + (editTextAmount != null ? editTextAmount.getText().toString() : "null"));

        // Get the selected currencies and amount entered by the user
        String fromCurrency = spinnerFromCurrency.getSelectedItem().toString();
        String toCurrency = spinnerToCurrency.getSelectedItem().toString();
        double amount;

        if (editTextAmount == null || editTextAmount.getText().toString().trim().isEmpty()) {
            // Show pop-up indicating empty amount
            Log.d("ConvertCurrency", "Empty or null amount entered");
            Toast.makeText(MainActivity.this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if amount is valid
        try {
            amount = Double.parseDouble(editTextAmount.getText().toString());
        } catch (NumberFormatException e) {
            // Handle invalid input
            Toast.makeText(MainActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch exchange rates asynchronously
        fetchExchangeRates(new ExchangeRatesCallback() {
            @Override
            public void onSuccess(ExchangeRatesResponse exchangeRatesResponse) {
                // Get the exchange rates
                Map<String, Double> exchangeRates = exchangeRatesResponse.getExchangeRates();

                // Check if both currencies are available in exchange rates
                if (exchangeRates.containsKey(fromCurrency) && exchangeRates.containsKey(toCurrency)) {
                    // Perform currency conversion
                    double fromRate = exchangeRates.get(fromCurrency);
                    double toRate = exchangeRates.get(toCurrency);
                    double convertedAmount = (amount / fromRate) * toRate;

                    // Find the EditText for converted amount
                    EditText editTextConvertedAmount = findViewById(R.id.editTextConvertedAmount);

                    // Set the text of the converted amount EditText
                    editTextConvertedAmount.setText(String.format(Locale.getDefault(), "%.2f %s", convertedAmount, toCurrency));
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

    // Define a callback interface for handling exchange rates response
    interface ExchangeRatesCallback {
        void onSuccess(ExchangeRatesResponse exchangeRatesResponse);
        void onFailure(String errorMessage);
    }

    // Method to fetch exchange rates asynchronously
    private void fetchExchangeRates(ExchangeRatesCallback callback) {
        exchangeRatesManager.getLatestExchangeRates(new Callback<ExchangeRatesResponse>() {
            @Override
            public void onResponse(Call<ExchangeRatesResponse> call, Response<ExchangeRatesResponse> response) {
                if (response.isSuccessful()) {
                    ExchangeRatesResponse ratesResponse = response.body();
                    Log.d("APIResponse", "Response Body: " + ratesResponse.toString());
                    if (ratesResponse != null) {
                        Log.d("APIResponse", "Base Currency: " + ratesResponse.getBaseCurrency());
                        Log.d("APIResponse", "Exchange Rates: " + ratesResponse.getExchangeRates());
                        callback.onSuccess(ratesResponse);
                    } else {
                        callback.onFailure("Response body is null");
                    }
                } else {
                    callback.onFailure("Failed to fetch exchange rates. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ExchangeRatesResponse> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }
    private void fetchExchangeRates() {
        exchangeRatesManager.getLatestExchangeRates(new Callback<ExchangeRatesResponse>() {
            @Override
            public void onResponse(Call<ExchangeRatesResponse> call, Response<ExchangeRatesResponse> response) {
                if (response.isSuccessful()) {
                    ExchangeRatesResponse ratesResponse = response.body();
                    Log.d("APIResponse", "Response Body: " + ratesResponse.toString());
                    if (ratesResponse != null) {
                        Log.d("APIResponse", "Base Currency: " + ratesResponse.getBaseCurrency());
                        Log.d("APIResponse", "Exchange Rates: " + ratesResponse.getExchangeRates());
                    } else {
                        Log.e("APIResponse", "Response body is null");
                    }
                    updateUI(ratesResponse);
                } else {
                    Log.e("FetchRatesError", "Failed to fetch exchange rates. Error code: " + response.code());
                    Log.e("FetchRatesError", "Error body: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ExchangeRatesResponse> call, Throwable t) {
                Log.e("FetchRatesError", "Network error: " + t.getMessage());
            }
        });
    }

    private void updateUI(ExchangeRatesResponse ratesResponse) {
        for (String currencyCode : currencyCodes) {
            // Use Retrofit to fetch currency names
            getCurrencyName(currencyCode, new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> currencyNames = response.body();
                        if (currencyNames != null) {
                            String name = currencyNames.get(currencyCode); // Get the name for this currency code
                            double rate = ratesResponse.getExchangeRates().getOrDefault(currencyCode, 0.0); // Get the rate for this currency code
                            int iconResourceId = getIconResourceIdForCurrency(currencyCode); // Get the icon resource ID for this currency code

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
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Log.e("CurrencyResponse", "Failed to fetch currency names: " + t.getMessage());
                }
            });
        }
    }


    // Define a class to hold currency information including icon resource ID
    class CurrencyInfo {
        private String name;
        private int iconResourceId;

        public CurrencyInfo(String name, int iconResourceId) {
            this.name = name;
            this.iconResourceId = iconResourceId;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
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
