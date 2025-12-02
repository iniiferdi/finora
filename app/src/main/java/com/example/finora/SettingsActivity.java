package com.example.finora;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        initNavbar();
        findViewById(R.id.btnDetailBack).setOnClickListener(v -> {
            finish();
        });

    }

    private void initNavbar() {

        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, HistoryActivity.class));
        });

        findViewById(R.id.navWallet).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, WalletActivity.class));
        });

        findViewById(R.id.navDash).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        });

        findViewById(R.id.navStats).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, StatisticsActivity.class));
        });

        findViewById(R.id.navSetting).setOnClickListener(v -> {
        });
    }
}
