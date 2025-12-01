package com.example.finora.data;

import androidx.room.Ignore;

public class MonthlyTotal {
    public String month;
    public Double total;
    public String type; 

    public MonthlyTotal(String month, Double total, String type) {
        this.month = month;
        this.total = total;
        this.type = type;
    }

    @Ignore
    public MonthlyTotal(String month, Double total) {
        this.month = month;
        this.total = total;
        this.type = "UNKNOWN";
    }
}
