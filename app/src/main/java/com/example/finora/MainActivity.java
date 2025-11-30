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
import com.example.finora.data.TransactionDao;
import com.example.finora.data.TransactionEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TransactionAdapter adapter;
    private TransactionDao transactionDao;

    private String formatRupiah(int value) {
        return String.format(Locale.US, "%,d", value).replace(",", ".");
    }

    private void loadBalance() {
        BalanceDao balanceDao = AppDatabase.getInstance(this).balanceDao();
        Integer balance = balanceDao.getBalance();

        if (balance == null) balance = 0;

        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTotalAmount.setText("Rp " + formatRupiah(balance));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayTransactions();
        loadBalance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View overlay = findViewById(R.id.searchOverlay);
        View btnSearch = findViewById(R.id.btnSearch);
        View btnClose = findViewById(R.id.btnCloseSearch);
        EditText etSearch = findViewById(R.id.etSearch);
        RecyclerView rvSearch = findViewById(R.id.rvSearchResults);

        TransactionAdapter searchAdapter = new TransactionAdapter();
        rvSearch.setLayoutManager(new LinearLayoutManager(this));
        rvSearch.setAdapter(searchAdapter);

// OPEN SEARCH OVERLAY
        btnSearch.setOnClickListener(v -> {
            overlay.setVisibility(View.VISIBLE);
            overlay.setAlpha(0f);
            overlay.setTranslationY(-80f);

            overlay.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(180)
                    .start();

            etSearch.requestFocus();
        });

// CLOSE SEARCH
        btnClose.setOnClickListener(v -> {
            overlay.animate()
                    .alpha(0f)
                    .translationY(-80f)
                    .setDuration(180)
                    .withEndAction(() -> overlay.setVisibility(View.GONE))
                    .start();
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();

                List<TransactionEntity> all = transactionDao.getAll();
                List<TransactionEntity> filtered = new java.util.ArrayList<>();

                for (TransactionEntity t : all) {
                    if (t.title.toLowerCase().contains(query) ||
                            t.type.toLowerCase().contains(query)) {
                        filtered.add(t);
                    }
                }

                searchAdapter.setData(filtered);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });



        BalanceDao balanceDao = AppDatabase.getInstance(this).balanceDao();
        Integer balance = balanceDao.getBalance();
        if (balance == null) {
            balanceDao.insert(new BalanceEntity(0));
        }

        transactionDao = AppDatabase.getInstance(this).transactionDao();

        RecyclerView rvToday = findViewById(R.id.rvTodayTransactions);
        adapter = new TransactionAdapter();

        adapter.setOnDeleteClickListener(item -> {
            AppDatabase db = AppDatabase.getInstance(this);

            TransactionEntity old = db.transactionDao().getById(item.id);

            if (old == null) return;

            BalanceDao bDao = db.balanceDao();
            Integer currentBalance = bDao.getBalance();
            if (currentBalance == null) currentBalance = 0;

            if (old.type.equalsIgnoreCase("IN")) {
                currentBalance = currentBalance - old.amount;
            } else {
                currentBalance = currentBalance + old.amount;
            }

            bDao.updateBalance(currentBalance);
            db.transactionDao().deleteById(item.id);

            loadTodayTransactions();
            loadBalance();
        });

        rvToday.setAdapter(adapter);
        rvToday.setLayoutManager(new LinearLayoutManager(this));

        loadTodayTransactions();

        findViewById(R.id.navAdd).setOnClickListener(v -> showAddModal());
    }

    private void loadTodayTransactions() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<TransactionEntity> data = transactionDao.getToday(today);
        adapter.setData(data);

        if (data.isEmpty()) {
            BalanceDao balanceDao = AppDatabase.getInstance(this).balanceDao();
            balanceDao.updateBalance(0);
            loadBalance();
        }
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
