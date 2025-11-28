package com.example.finora.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface BalanceDao {

    @Query("SELECT balance FROM balance WHERE id = 1")
    Integer getBalance();

    @Insert
    void insert(BalanceEntity balanceEntity);

    @Query("UPDATE balance SET balance = :newBalance WHERE id = 1")
    void updateBalance(int newBalance);
}
