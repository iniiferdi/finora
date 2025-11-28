package com.example.finora.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "balance")
public class BalanceEntity {

    @PrimaryKey(autoGenerate = false)
    public int id = 1;

    public int balance;

    public BalanceEntity(int balance) {
        this.balance = balance;
    }
}
