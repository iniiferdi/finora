package com.example.finora;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initNavbar();

        findViewById(R.id.btnDetailBack).setOnClickListener(v -> {
            finish();
        });
    }

    private void initNavbar() {

        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(WalletActivity.this, HistoryActivity.class));
        });

        findViewById(R.id.navWallet).setOnClickListener(v -> {
        });

        findViewById(R.id.navDash).setOnClickListener(v -> {
            startActivity(new Intent(WalletActivity.this, MainActivity.class));
        });

        findViewById(R.id.navStats).setOnClickListener(v -> {
            startActivity(new Intent(WalletActivity.this, StatisticsActivity.class));
        });

        findViewById(R.id.navSetting).setOnClickListener(v -> {
            startActivity(new Intent(WalletActivity.this, SettingsActivity.class));
        });
    }
}


