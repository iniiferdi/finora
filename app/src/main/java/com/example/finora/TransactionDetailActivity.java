package com.example.finora;

import android.content.Intent;
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

        initNavbar();




        tvTitle.setText(t.title);
        tvDate.setText(t.date);
        tvType.setText(t.type.equalsIgnoreCase("INCOME") ? "INCOME" : "EXPENSE");

        String formattedAmount = "Rp " + String.format("%,d", t.amount)
                .replace(",", ".");

        tvAmount.setText(formattedAmount);

        if (t.type.equalsIgnoreCase("INCOME"))
            iconType.setImageResource(R.drawable.ic_income);
        else
            iconType.setImageResource(R.drawable.ic_expense);

        btnBack.setOnClickListener(v -> finish());
    }

    private void initNavbar() {

        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(TransactionDetailActivity.this, HistoryActivity.class));
        });

        findViewById(R.id.navWallet).setOnClickListener(v -> {
            startActivity(new Intent(TransactionDetailActivity.this, WalletActivity.class));
        });

        findViewById(R.id.navDash).setOnClickListener(v -> {
            startActivity(new Intent(TransactionDetailActivity.this, MainActivity.class));
        });

        findViewById(R.id.navStats).setOnClickListener(v -> {
            startActivity(new Intent(TransactionDetailActivity.this, StatisticsActivity.class));
        });

        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(TransactionDetailActivity.this, SettingsActivity.class));
        });
    }
}
