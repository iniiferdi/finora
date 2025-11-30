package com.example.finora.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class StatisticsViewModel extends AndroidViewModel {
    private StatisticsRepository repository;
    private MutableLiveData<Double> monthlyIncome = new MutableLiveData<>();
    private MutableLiveData<Double> monthlyExpense = new MutableLiveData<>();
    private MutableLiveData<Double> totalBalance = new MutableLiveData<>();

    public StatisticsViewModel(Application application) {
        super(application);
        repository = new StatisticsRepository(application);
        loadStatistics();
    }

    private void loadStatistics() {
        monthlyIncome.setValue(repository.getMonthlyIncome());
        monthlyExpense.setValue(repository.getMonthlyExpense());
        totalBalance.setValue(repository.getTotalBalance());
    }

    public LiveData<Double> getMonthlyIncome() {
        return monthlyIncome;
    }

    public LiveData<Double> getMonthlyExpense() {
        return monthlyExpense;
    }

    public LiveData<Double> getTotalBalance() {
        return totalBalance;
    }

    public void refreshData() {
        loadStatistics();
    }
}