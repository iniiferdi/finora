package com.example.finora;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.finora.data.StatisticsViewModel;
import java.text.NumberFormat;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvIncome, tvExpense, tvBalance, tvSavings;
    private StatisticsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tvSavings = findViewById(R.id.tvSavings);

        // Observe data dari ViewModel
        observeData();
    }

    private void observeData() {
        // Observe pemasukan bulan ini
        viewModel.getMonthlyIncome().observe(this, income -> {
            tvIncome.setText(formatCurrency(income));
            calculateSavings();
        });

        // Observe pengeluaran bulan ini
        viewModel.getMonthlyExpense().observe(this, expense -> {
            tvExpense.setText(formatCurrency(expense));
            calculateSavings();
        });

        // Observe total saldo
        viewModel.getTotalBalance().observe(this, balance -> {
            tvBalance.setText(formatCurrency(balance));
        });
    }

    private void calculateSavings() {
        Double income = viewModel.getMonthlyIncome().getValue();
        Double expense = viewModel.getMonthlyExpense().getValue();

        if (income != null && expense != null) {
            double savings = income - expense;  // ganti Double jadi double
            tvSavings.setText(formatCurrency(savings));

            // Warna hijau untuk positif, merah untuk negatif
            int color = savings >= 0 ?
                    ContextCompat.getColor(this, android.R.color.holo_green_dark) :
                    ContextCompat.getColor(this, android.R.color.holo_red_dark);
            tvSavings.setTextColor(color);
        }
    }

    private String formatCurrency(Double amount) {
        if (amount == null) amount = 0.0;
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika activity resume
        viewModel.refreshData();
    }
}