package com.example.finora;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TransactionDao transactionDao;
    private TransactionAdapter adapter;
    private WaveChartView waveChart;
    private LinearLayout monthLabelsContainer;

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
        monthLabelsContainer = findViewById(R.id.monthLabelsContainer);

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
        String[] months = new String[6];

        // Initialize with placeholder
        for (int i = 0; i < 6; i++) months[i] = "";

        if (totals != null && !totals.isEmpty()) {
            double max = 0;
            for (MonthlyTotal mt : totals) {
                if (mt.total > max) max = mt.total;
            }

            // Determine number of points to show (max 6)
            // The query returns DESC, so index 0 is the latest month.
            // WaveChartView usually draws left to right.
            // If we want the latest month on the RIGHT, we need to reverse the data.
            // Let's assume we want to show chronological order (Oldest -> Newest)
            
            int count = Math.min(totals.size(), 6);
            
            // Fill points array from right to left? Or just first N points?
            // Usually charts show [Month-5, Month-4, ... Month-0]
            // totals is ORDER BY month DESC -> [Current, Prev, PrevPrev...]
            
            // We need to reverse the order to get chronological
            for (int i = 0; i < count; i++) {
                // Map totals index (0=latest) to points index (count-1-i = position from right)
                // Example: count=2. totals[0]=Oct, totals[1]=Sep.
                // We want: points[0]=Sep, points[1]=Oct.
                // So:
                // points[count - 1 - i]
                
                MonthlyTotal mt = totals.get(i);
                double val = mt.total;
                
                int targetIndex = count - 1 - i;
                
                if (max > 0) {
                    points[targetIndex] = (float) (0.9f - (val / max * 0.6f));
                }
                
                // Parse month string "YYYY-MM" -> "Mon" (e.g., "Oct")
                String monthName = "";
                try {
                    String monthNum = mt.month.substring(5, 7);
                    int m = Integer.parseInt(monthNum);
                    // DateFormatSymbols returns 0-based array sometimes, but months are 1-12 usually or 0-11.
                    // shortMonths are 0-based (0=Jan).
                    String[] shortMonths = new DateFormatSymbols(Locale.US).getShortMonths();
                    if (m >= 1 && m <= 12) {
                        monthName = shortMonths[m - 1];
                    }
                } catch (Exception e) {
                    monthName = mt.month;
                }
                months[targetIndex] = monthName;
            }
            
            // Resize points array if we have fewer than 6 data points?
            // The WaveChartView expects a fixed array or handles whatever size?
            // Let's resize to match actual data count if count < 6, OR just stick to 6 and fill 0s.
            // The user probably wants a nice curve.
            // If we only have 1 month of data, a wave chart is just a dot.
            // Let's stick to the logic: if we have data, use it.
            // However, WaveChartView points array size determines the number of steps.
            
            float[] finalPoints = new float[count];
            String[] finalMonths = new String[count];
            
            for(int i=0; i<count; i++) {
                finalPoints[i] = points[i];
                finalMonths[i] = months[i];
            }
            
            waveChart.setPoints(finalPoints);
            updateMonthLabels(finalMonths);
            
        } else {
            // No data
             waveChart.setPoints(new float[]{0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f});
             // Clear labels or show empty
             updateMonthLabels(new String[]{});
        }
    }

    private void updateMonthLabels(String[] months) {
        monthLabelsContainer.removeAllViews();
        if (months == null || months.length == 0) return;

        for (int i = 0; i < months.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(months[i]);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0, 
                    ViewGroup.LayoutParams.WRAP_CONTENT, 
                    1.0f
            ));
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
            
            // Highlight the last one (current month)
            if (i == months.length - 1) {
                tv.setTypeface(null, Typeface.BOLD);
                tv.setTextColor(getResources().getColor(android.R.color.black));
            }
            
            monthLabelsContainer.addView(tv);
        }
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
