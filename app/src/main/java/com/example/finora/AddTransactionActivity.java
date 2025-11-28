package com.example.finora;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finora.data.AppDatabase;
import com.example.finora.data.BalanceDao;
import com.example.finora.data.TransactionEntity;

import java.time.LocalDate;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView amountText, titleTransaction;
    private TextView expensesTab, incomeTab;
    private final StringBuilder input = new StringBuilder();

    private boolean isExpense = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initViews();
        setupTabSwitch();
        setupKeypad();
        generateDateItems();
        String title = getIntent().getStringExtra("transaction_title");
        if (title != null && !title.isEmpty()) {
            titleTransaction.setText(title);
        }
    }
    private void initViews() {
        amountText = findViewById(R.id.amountText);
        titleTransaction = findViewById(R.id.titleTransaction);
        expensesTab = findViewById(R.id.expensesTab);
        incomeTab = findViewById(R.id.incomeTab);
    }
    private void setupTabSwitch() {
        setTabState(true); // Default: EXPENSE

        expensesTab.setOnClickListener(v -> setTabState(true));
        incomeTab.setOnClickListener(v -> setTabState(false));
    }

    private void setTabState(boolean expenseActive) {
        isExpense = expenseActive;

        if (expenseActive) {
            expensesTab.setBackgroundResource(R.drawable.tab_selected);
            expensesTab.setTextColor(Color.WHITE);

            incomeTab.setBackground(null);
            incomeTab.setTextColor(Color.GRAY);
        } else {
            incomeTab.setBackgroundResource(R.drawable.tab_selected);
            incomeTab.setTextColor(Color.WHITE);

            expensesTab.setBackground(null);
            expensesTab.setTextColor(Color.GRAY);
        }
    }
    private void setupKeypad() {
        int[] ids = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        for (int id : ids) {
            findViewById(id).setOnClickListener(v -> {
                String value = ((TextView) v).getText().toString();
                updateInput(value);
            });
        }
        findViewById(R.id.btnDel).setOnClickListener(v -> {
            if (input.length() > 0) {
                input.deleteCharAt(input.length() - 1);
                updateDisplay();
            }
        });
        findViewById(R.id.btnConfirm).setOnClickListener(v -> saveTransaction());
    }
    private void updateInput(String value) {
        if (value.equals("0") && input.length() == 0) return; // mencegah angka "0" di awal
        input.append(value);
        updateDisplay();
    }
    private String formatNumber(String number) {
        if (number.isEmpty()) return "0";

        try {
            long value = Long.parseLong(number);
            return String.format("%,d", value).replace(",", ".");
        } catch (Exception e) {
            return number;
        }
    }

    private void updateDisplay() {
        String raw = (input.length() == 0) ? "0" : input.toString();
        amountText.setText("Rp " + formatNumber(raw));
    }

    private void saveTransaction() {
        String amountStr = (input.length() == 0) ? "0" : input.toString();
        int amount = Integer.parseInt(amountStr); // input mentah → aman

        if (amount <= 0) {
            Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleTransaction.getText().toString();
        String type = isExpense ? "EXPENSE" : "INCOME";
        String date = LocalDate.now().toString();

        AppDatabase db = AppDatabase.getInstance(this);

        TransactionEntity transaction = new TransactionEntity(title, amount, type, date);
        db.transactionDao().insert(transaction);

        BalanceDao balanceDao = db.balanceDao();
        Integer currentBalance = balanceDao.getBalance();
        int newBalance = (currentBalance == null ? 0 : currentBalance)
                + (isExpense ? -amount : amount);

        balanceDao.updateBalance(newBalance);

        finish();
    }

    private void generateDateItems() {
        LinearLayout dateRow = findViewById(R.id.dateRow);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3);

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);

            View item = getLayoutInflater().inflate(R.layout.item_date, dateRow, false);
            TextView txt = item.findViewById(R.id.txtDate);

            txt.setText(
                    date.getDayOfMonth() + "\n" +
                            date.getDayOfWeek().toString().substring(0, 3).toLowerCase()
            );

            boolean isSelected = date.equals(today);

            item.setBackgroundResource(isSelected
                    ? R.drawable.date_item_selected_bg
                    : R.drawable.date_item_bg);

            txt.setTextColor(isSelected ? Color.WHITE : Color.BLACK);

            item.setOnClickListener(v -> updateSelectedDate(v, dateRow));

            dateRow.addView(item);
        }
    }

    private void updateSelectedDate(View selectedView, LinearLayout dateRow) {
        for (int i = 0; i < dateRow.getChildCount(); i++) {
            View child = dateRow.getChildAt(i);
            TextView txt = child.findViewById(R.id.txtDate);

            boolean isSelected = (child == selectedView);

            child.setBackgroundResource(isSelected
                    ? R.drawable.date_item_selected_bg
                    : R.drawable.date_item_bg);

            txt.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
        }
    }
}
