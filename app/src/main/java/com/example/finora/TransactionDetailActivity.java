package com.example.finora;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finora.data.AppDatabase;
import com.example.finora.data.TransactionEntity;

public class TransactionDetailActivity extends AppCompatActivity {

    TextView tvTitle, tvAmount, tvDate, tvType;
    ImageView btnBack, iconType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        int id = getIntent().getIntExtra("id", -1);
        if (id == -1) {
            finish();
            return;
        }

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAmount = findViewById(R.id.tvDetailAmount);
        tvDate = findViewById(R.id.tvDetailDate);
        tvType = findViewById(R.id.tvDetailType);

        btnBack = findViewById(R.id.btnDetailBack);
        iconType = findViewById(R.id.iconType);

        TransactionEntity t = AppDatabase.getInstance(this)
                .transactionDao()
                .getById(id);

        if (t == null) {
            finish();
            return;
        }

        tvTitle.setText(t.title);
        tvDate.setText(t.date);
        tvType.setText(t.type.equals("IN") ? "INCOME" : "EXPENSE");

        String formattedAmount = "Rp " + String.format("%,d", t.amount)
                .replace(",", ".");

        tvAmount.setText(formattedAmount);

        if (t.type.equals("IN"))
            iconType.setImageResource(R.drawable.ic_income);
        else
            iconType.setImageResource(R.drawable.ic_expense);

        btnBack.setOnClickListener(v -> finish());
    }
}
