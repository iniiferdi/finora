package com.example.finora;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.finora.data.AppDatabase;
import com.example.finora.data.TransactionDao;
import com.example.finora.data.StatisticsViewModel;
import com.example.finora.ui.chart.FinoraStatsChartView;

import java.time.LocalDate;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvIncome, tvExpense, tvBalance;
    private StatisticsViewModel viewModel;

    private TransactionDao dao;
    private FinoraStatsChartView chart;

    private final int MONTH_COUNT = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);


        dao = AppDatabase.getInstance(this).transactionDao();
        chart = findViewById(R.id.finoraChart);

        observeData();
        loadChartData();
    }

    private void loadChartData() {
        float[] incomeData = new float[MONTH_COUNT];
        float[] expenseData = new float[MONTH_COUNT];

        LocalDate now = LocalDate.now();

        for (int i = MONTH_COUNT - 1; i >= 0; i--) {
            LocalDate month = now.minusMonths(MONTH_COUNT - 1 - i);
            String monthYear = month.toString().substring(0, 7); // YYYY-MM

            Double monthlyIncome = dao.getMonthlyIncome(monthYear);
            Double monthlyExpense = dao.getMonthlyExpense(monthYear);

            incomeData[i] = monthlyIncome != null ? monthlyIncome.floatValue() : 0f;
            expenseData[i] = monthlyExpense != null ? monthlyExpense.floatValue() : 0f;
        }

        chart.setData(incomeData, expenseData);
    }

    private void observeData() {
        viewModel.getMonthlyIncome().observe(this, income -> {
            tvIncome.setText(formatRupiah(income));
        });

        viewModel.getMonthlyExpense().observe(this, expense -> {
            tvExpense.setText(formatRupiah(expense));
        });

        viewModel.getTotalBalance().observe(this, balance -> {
            tvBalance.setText(formatRupiah(balance));
        });
    }



    private String formatRupiah(double value) {
        int intValue = (int) value;
        return String.format(Locale.US, "%,d", intValue).replace(",", ".");
    }


    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshData();
        loadChartData();
    }
}
