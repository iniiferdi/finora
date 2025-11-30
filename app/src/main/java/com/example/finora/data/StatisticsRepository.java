package com.example.finora.data;

import android.app.Application;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatisticsRepository {
    private TransactionDao transactionDao;
    private BalanceDao balanceDao;
    private String currentMonthYear;

    public StatisticsRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        balanceDao = database.balanceDao();

        // Format bulan-tahun saat ini (YYYY-MM)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        currentMonthYear = sdf.format(new Date());
    }

    public Double getMonthlyIncome() {
        Double income = transactionDao.getMonthlyIncome(currentMonthYear);
        return income != null ? income : 0.0;
    }

    public Double getMonthlyExpense() {
        Double expense = transactionDao.getMonthlyExpense(currentMonthYear);
        return expense != null ? expense : 0.0;
    }

    public Double getTotalBalance() {
        Integer balance = balanceDao.getBalance();
        return balance != null ? balance.doubleValue() : 0.0;
    }
}