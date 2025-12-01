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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        findViewById(R.id.navSetting).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.navWallet).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WalletActivity.class);
            startActivity(intent);
        });



        loadTodayTransactions();
        loadBalance();
        loadChartData();

        findViewById(R.id.navStats).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });


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
        List<MonthlyTotal> allTotals = transactionDao.getAllMonthlyTotals();

        Map<String, Double> incomeMap = new HashMap<>();
        Map<String, Double> expenseMap = new HashMap<>();

        if (allTotals != null) {
            for (MonthlyTotal mt : allTotals) {
                if ("INCOME".equalsIgnoreCase(mt.type)) {
                    incomeMap.put(mt.month, mt.total);
                } else if ("EXPENSE".equalsIgnoreCase(mt.type)) {
                    expenseMap.put(mt.month, mt.total);
                }
            }
        }

        int numPoints = 6;
        float[] points = new float[numPoints];
        String[] displayMonths = new String[numPoints];

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -5);
        SimpleDateFormat sdfKey = new SimpleDateFormat("yyyy-MM", Locale.US);
        String[] shortMonths = new DateFormatSymbols(Locale.US).getShortMonths();

        double maxAbs = 0;
        double[] netValues = new double[numPoints];

        Calendar tempCal = (Calendar) calendar.clone();
        for (int i = 0; i < numPoints; i++) {
            String key = sdfKey.format(tempCal.getTime());
            double inc = incomeMap.getOrDefault(key, 0.0);
            double exp = expenseMap.getOrDefault(key, 0.0);
            double net = inc - exp;
            netValues[i] = net;

            if (Math.abs(net) > maxAbs) {
                maxAbs = Math.abs(net);
            }
            tempCal.add(Calendar.MONTH, 1);
        }

        if (maxAbs == 0) maxAbs = 1;

        for (int i = 0; i < numPoints; i++) {
            points[i] = (float) (0.5f - (netValues[i] / maxAbs * 0.4f));
            
            int monthIndex = calendar.get(Calendar.MONTH);
            displayMonths[i] = shortMonths[monthIndex];
            calendar.add(Calendar.MONTH, 1);
        }

        waveChart.setPoints(points);
        updateMonthLabels(displayMonths);
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
