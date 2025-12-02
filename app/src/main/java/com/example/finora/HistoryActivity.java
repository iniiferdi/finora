package com.example.finora;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finora.data.AppDatabase;
import com.example.finora.data.TransactionDao;
import com.example.finora.data.TransactionEntity;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TransactionAdapter adapter;
    private TransactionDao transactionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter();
        rvHistory.setAdapter(adapter);

        transactionDao = AppDatabase.getInstance(this).transactionDao();

        loadAllHistory();
        initNavbar();

        findViewById(R.id.btnDetailBack).setOnClickListener(v -> {
            finish();
        });

    }

    private void initNavbar() {

        findViewById(R.id.navHome).setOnClickListener(v -> {
        });

        findViewById(R.id.navWallet).setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, WalletActivity.class));
        });

        findViewById(R.id.navDash).setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, MainActivity.class));
        });

        findViewById(R.id.navStats).setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, StatisticsActivity.class));
        });

        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, SettingsActivity.class));
        });
    }

    private void loadAllHistory() {
        List<TransactionEntity> data = transactionDao.getAll();
        adapter.setData(data);
    }
}
