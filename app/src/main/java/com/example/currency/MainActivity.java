package com.example.currency;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    List<String> currencyCodes = Arrays.asList("USD", "TRY", "EUR", "GBP", "JPY", "CAD", "AUD", "CNY");
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

        fetchExchangeRates();

        setupSpinnersAndButton();

        initializeCurrencyList();

        editTextAmount = findViewById(R.id.editTextAmount);

        editTextAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard();
                return true;
            }
            return false;
        });
        hideSystemBars();

    }

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

    private void setupSpinnersAndButton() {
        spinnerFromCurrency = findViewById(R.id.spinnerFromCurrency);
        spinnerToCurrency = findViewById(R.id.spinnerToCurrency);

        ArrayAdapter<String> spinnerAdapterFromCurrency = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencyCodes);
        spinnerAdapterFromCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromCurrency.setAdapter(spinnerAdapterFromCurrency);

        ArrayAdapter<String> spinnerAdapterToCurrency = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencyCodes);
        spinnerAdapterToCurrency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToCurrency.setAdapter(spinnerAdapterToCurrency);

        buttonConvert = findViewById(R.id.buttonConvert);

        buttonConvert.setOnClickListener(v -> convertCurrency());
    }



    private void initializeCurrencyList() {
        for (String currencyCode : currencyCodes) {
            currencyList.add(new Currency(0, "", 0.0, currencyCode));
        }
    }

    private int getIconResourceIdForCurrency(String currencyCode) {
        int iconResourceId;
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
            case "USD":
                iconResourceId = R.drawable.usd;
                break;
            default:
                iconResourceId = R.drawable.turk;
                break;
        }
        return iconResourceId;
    }
    private void convertCurrency() {

        String fromCurrency = spinnerFromCurrency.getSelectedItem().toString();
        String toCurrency = spinnerToCurrency.getSelectedItem().toString();
        double amount;

        if (editTextAmount == null || editTextAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(MainActivity.this, "Lütfen bir miktar girin.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            amount = Double.parseDouble(editTextAmount.getText().toString());
        } catch (NumberFormatException e) {

            Toast.makeText(MainActivity.this, "Lütfen geçerli bir miktar girin.", Toast.LENGTH_SHORT).show();
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
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Seçilen para birimleri için döviz kurları mevcut değil.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, "Döviz kurları alınamadı: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }
    interface ExchangeRatesCallback {
        void onSuccess(ExchangeRatesResponse exchangeRatesResponse);
        void onFailure(String errorMessage);
    }
    private void fetchExchangeRates(ExchangeRatesCallback callback) {
        exchangeRatesManager.getLatestExchangeRates(new Callback<ExchangeRatesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExchangeRatesResponse> call, @NonNull Response<ExchangeRatesResponse> response) {
                if (response.isSuccessful()) {
                    ExchangeRatesResponse ratesResponse = response.body();
                    if (ratesResponse != null) {
                        callback.onSuccess(ratesResponse);
                    } else {
                        callback.onFailure("Döviz kuru yanıtı işlenemedi.");
                    }
                } else {
                    callback.onFailure("Döviz kurları alınamadı. Hata kodu: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ExchangeRatesResponse> call, @NonNull Throwable t) {
                callback.onFailure("Ağ hatası: " + t.getMessage());
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

                            for (Currency currency : currencyList) {
                                if (currency.getCode().equals(currencyCode)) {
                                    currency.setName(name);
                                    currency.setRate(rate);
                                    currency.setIconResourceId(iconResourceId);
                                    break;
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                }
            });
        }
    }
    public void getCurrencyName(String currencyCode, Callback<Map<String, String>> callback) {
        CurrencyNamesService service = RetrofitClient.getRetrofitInstance().create(CurrencyNamesService.class);
        Call<Map<String, String>> call = service.getCurrencyNames(currencyCode);
        call.enqueue(callback);
    }

}
