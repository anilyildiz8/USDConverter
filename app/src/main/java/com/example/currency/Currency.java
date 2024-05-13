package com.example.currency;

public class Currency {
    private int iconResourceId;
    private String name;
    private double rate;
    private final String code;


    public Currency(int iconResourceId, String name, double rate, String code) {
        this.iconResourceId = iconResourceId;
        this.name = name;
        this.rate = rate;
        this.code = code;
    }

    // Add a public getter method to retrieve the resource ID for the currency icon
    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    // Add getter and setter methods for other fields (name, rate, code) if needed
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getCode() {
        return code;
    }

}
