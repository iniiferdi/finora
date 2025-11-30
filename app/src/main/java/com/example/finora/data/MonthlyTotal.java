package com.example.finora.data;

public class MonthlyTotal {
    public String month;
    public Double total;

    public MonthlyTotal(String month, Double total) {
        this.month = month;
        this.total = total;
    }

    public String getMonth() { return month; }
    public Double getTotal() { return total; }
}
