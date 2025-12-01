package com.example.finora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finora.data.AppDatabase;
import com.example.finora.data.BalanceDao;
import com.example.finora.data.BalanceEntity;
import com.example.finora.data.MonthlyTotal;
import com.example.finora.data.TransactionDao;
import com.example.finora.data.TransactionEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TransactionDao transactionDao;
    private TransactionAdapter adapter;
    private WaveChartView waveChart;

    // ========================================================================
    // Utility
    // ========================================================================
    private String formatRupiah(int value) {
        return String.format(Locale.US, "%,d", value).replace(",", ".");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        transactionDao = AppDatabase.getInstance(this).transactionDao();
        ensureBalanceInitialized();

        waveChart = findViewById(R.id.waveChart);

        initSearchOverlay();
        initTodayList();

        loadTodayTransactions();
        loadBalance();
        loadChartData();

        findViewById(R.id.navAdd).setOnClickListener(v -> showAddModal());
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayTransactions();
        loadBalance();
        loadChartData();
    }

    private void ensureBalanceInitialized() {
        BalanceDao balanceDao = AppDatabase.getInstance(this).balanceDao();
        if (balanceDao.getBalance() == null) {
            balanceDao.insert(new BalanceEntity(0));
        }
    }

    private void loadBalance() {
        BalanceDao balanceDao = AppDatabase.getInstance(this).balanceDao();
        Integer balance = balanceDao.getBalance();
        if (balance == null) balance = 0;

        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTotalAmount.setText("Rp " + formatRupiah(balance));
    }

    private void loadChartData() {
        List<MonthlyTotal> totals = transactionDao.getMonthlyTotals("EXPENSE");

        // Default flat line at bottom
        float[] points = new float[]{0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f};

        if (totals != null && !totals.isEmpty()) {
            double max = 0;
            for (MonthlyTotal mt : totals) {
                if (mt.total > max) max = mt.total;
            }

            // Map up to 6 months of data
            int count = Math.min(totals.size(), 6);
            for (int i = 0; i < count; i++) {
                double val = totals.get(i).total;
                if (max > 0) {
                    // 0.9f is bottom, 0.3f is top
                    points[i] = (float) (0.9f - (val / max * 0.6f));
                }
            }
        }
        waveChart.setPoints(points);
    }

    private void initTodayList() {
        RecyclerView rvToday = findViewById(R.id.rvTodayTransactions);
        adapter = new TransactionAdapter();

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(this, TransactionDetailActivity.class);
            intent.putExtra("id", item.id);
            startActivity(intent);
        });
        adapter.setOnDeleteClickListener(item -> deleteTransaction(item.id));

        rvToday.setAdapter(adapter);
        rvToday.setLayoutManager(new LinearLayoutManager(this));
    }

    private void deleteTransaction(int id) {
        AppDatabase db = AppDatabase.getInstance(this);

        TransactionEntity old = db.transactionDao().getById(id);
        if (old == null) return;

        BalanceDao bDao = db.balanceDao();
        Integer current = bDao.getBalance();
        if (current == null) current = 0;

        if (old.type.equalsIgnoreCase("INCOME")) {
            current -= old.amount;
        } else {
            current += old.amount;
        }

        bDao.updateBalance(current);
        db.transactionDao().deleteById(id);

        loadTodayTransactions();
        loadBalance();
        loadChartData();
    }

    private void loadTodayTransactions() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<TransactionEntity> data = transactionDao.getToday(today);

        adapter.setData(data);
    }

    private void initSearchOverlay() {
        View overlay = findViewById(R.id.searchOverlay);
        View btnSearch = findViewById(R.id.btnSearch);
        View btnClose = findViewById(R.id.btnCloseSearch);
        EditText etSearch = findViewById(R.id.etSearch);
        RecyclerView rvSearch = findViewById(R.id.rvSearchResults);

        TransactionAdapter searchAdapter = new TransactionAdapter();
        rvSearch.setLayoutManager(new LinearLayoutManager(this));
        rvSearch.setAdapter(searchAdapter);

        btnSearch.setOnClickListener(v -> showSearchOverlay(overlay, etSearch));
        btnClose.setOnClickListener(v -> hideSearchOverlay(overlay));
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearchResult(s.toString(), searchAdapter);
            }

            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void showSearchOverlay(View overlay, EditText etSearch) {
        overlay.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);
        overlay.setTranslationY(-80f);

        overlay.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .start();

        etSearch.requestFocus();
    }

    private void hideSearchOverlay(View overlay) {
        overlay.animate()
                .alpha(0f)
                .translationY(-80f)
                .setDuration(180)
                .withEndAction(() -> overlay.setVisibility(View.GONE))
                .start();
    }

    private void filterSearchResult(String query, TransactionAdapter searchAdapter) {
        query = query.toLowerCase();
        List<TransactionEntity> all = transactionDao.getAll();
        List<TransactionEntity> filtered = new ArrayList<>();

        for (TransactionEntity t : all) {
            if (t.title.toLowerCase().contains(query) || t.type.toLowerCase().contains(query)) {
                filtered.add(t);
            }
        }

        searchAdapter.setData(filtered);
    }

    private void showAddModal() {
        BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.modal_add, null);

        TextInputEditText inputDetail = view.findViewById(R.id.inputDetail);

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btnNext).setOnClickListener(v -> {
            String title = inputDetail.getText() != null ? inputDetail.getText().toString().trim() : "";

            if (title.isEmpty()) {
                inputDetail.setError("Judul tidak boleh kosong");
                return;
            }

            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            intent.putExtra("transaction_title", title);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }
}
