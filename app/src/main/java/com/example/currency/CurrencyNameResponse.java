package com.example.currency;

import com.google.gson.annotations.SerializedName;

public class CurrencyNameResponse {
    @SerializedName("name") // Adjusted field name to match the API response
    private String name;

    // Constructor, getters, and setters

    public String getName() {
        return name;
    }

}
