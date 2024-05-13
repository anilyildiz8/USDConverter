package com.example.currency;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {

    private final Context context;
    private final List<Currency> currencyList;

    public CurrencyAdapter(Context context, List<Currency> currencyList) {
        this.context = context;
        this.currencyList = currencyList;
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_currency, parent, false);
        return new CurrencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        Currency currency = currencyList.get(position);
        holder.currencyIcon.setImageResource(currency.getIconResourceId());
        holder.currencyName.setText(currency.getName());
        holder.currencyRate.setText(String.format(Locale.getDefault(), "%.2f", currency.getRate()));
    }


    @Override
    public int getItemCount() {
        return currencyList.size();
    }

    static class CurrencyViewHolder extends RecyclerView.ViewHolder {
        ImageView currencyIcon;
        TextView currencyName;
        TextView currencyRate;

        CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            currencyIcon = itemView.findViewById(R.id.currencyIcon);
            currencyName = itemView.findViewById(R.id.currencyName);
            currencyRate = itemView.findViewById(R.id.currencyRate);
        }
    }
}

