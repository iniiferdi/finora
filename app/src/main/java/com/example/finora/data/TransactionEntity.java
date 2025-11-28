package com.example.finora.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public int amount;
    public String type;
    public String date;

    public TransactionEntity(String title, int amount, String type, String date) {
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }
}

