package com.example.finora;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finora.data.TransactionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionEntity> list = new ArrayList<>();
    private OnDeleteClickListener deleteListener;

    // -----------------------------
    // Callback delete ke MainActivity
    // -----------------------------
    public interface OnDeleteClickListener {
        void onDelete(TransactionEntity item);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    // -----------------------------
    // Set data list
    // -----------------------------
    public void setData(List<TransactionEntity> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    // -----------------------------
    // Create ViewHolder
    // -----------------------------
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    // -----------------------------
    // Bind data ke tiap row
    // -----------------------------
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionEntity item = list.get(position);

        holder.tvTitle.setText(item.title);
        holder.tvCategory.setText(item.type);
        holder.tvAmount.setText("Rp " + formatRupiah(item.amount));

        // Awal icon delete disembunyikan
        holder.iconDelete.setVisibility(View.GONE);

        // Long-press memunculkan delete button
        holder.itemView.setOnLongClickListener(v -> {
            if (holder.iconDelete.getVisibility() == View.GONE) {
                holder.iconDelete.setVisibility(View.VISIBLE);
                holder.iconDelete.setAlpha(0f);
                holder.iconDelete.setTranslationX(80f); // posisi dari kanan

                holder.iconDelete.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(180)
                        .start();
            }
            return true;
        });


        // Klik tombol delete
        holder.iconDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // -----------------------------
    // ViewHolder
    // -----------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount;
        ImageView iconDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            iconDelete = itemView.findViewById(R.id.iconDelete);
        }
    }

    // -----------------------------
    // Format rupiah
    // -----------------------------
    private String formatRupiah(int value) {
        return String.format(Locale.US, "%,d", value).replace(",", ".");
    }
}
