package com.example.finora.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(TransactionEntity transaction);

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    List<TransactionEntity> getAll();

    @Query("SELECT * FROM transactions WHERE date = :today ORDER BY id DESC")
    List<TransactionEntity> getToday(String today);
}

