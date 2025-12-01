package com.example.finora.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    TransactionEntity getById(int id);

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    List<TransactionEntity> getAll();

    @Query("SELECT * FROM transactions WHERE date = :today ORDER BY id DESC")
    List<TransactionEntity> getToday(String today);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    Double getTotalIncome();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    Double getTotalExpense();

    @Query("SELECT substr(date, 1, 7) AS month, SUM(amount) AS total, type " +
            "FROM transactions " +
            "GROUP BY month, type " +
            "ORDER BY month DESC")
    List<MonthlyTotal> getAllMonthlyTotals();

    @Query("SELECT substr(date, 1, 7) AS month, SUM(amount) AS total " +
             "FROM transactions " +
             "WHERE type = :type " +
             "GROUP BY month " +
             "ORDER BY month DESC LIMIT 6")
    List<MonthlyTotal> getMonthlyTotals(String type);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND substr(date, 1, 7) = :monthYear")
    Double getMonthlyIncome(String monthYear);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND substr(date, 1, 7) = :monthYear")
    Double getMonthlyExpense(String monthYear);
}
