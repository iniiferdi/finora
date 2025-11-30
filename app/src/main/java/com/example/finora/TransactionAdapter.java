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

    public interface OnDeleteClickListener {
        void onDelete(TransactionEntity item);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setData(List<TransactionEntity> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionEntity item = list.get(position);

        holder.tvTitle.setText(item.title);
        holder.tvCategory.setText(item.type);
        holder.tvAmount.setText("Rp " + formatRupiah(item.amount));

        holder.normalLayout.setVisibility(View.VISIBLE);
        holder.deleteLayout.setVisibility(View.GONE);

        holder.itemView.setOnLongClickListener(v -> {
            holder.normalLayout.setVisibility(View.GONE);
            holder.deleteLayout.setVisibility(View.VISIBLE);
            return true;
        });

        holder.iconDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(item);
        });

        holder.deleteLayout.setOnClickListener(v -> {
            holder.deleteLayout.setVisibility(View.GONE);
            holder.normalLayout.setVisibility(View.VISIBLE);
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount, tvDeleteText;
        View normalLayout, deleteLayout;
        ImageView iconDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            normalLayout = itemView.findViewById(R.id.normalLayout);
            deleteLayout = itemView.findViewById(R.id.deleteLayout);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);

            iconDelete = itemView.findViewById(R.id.iconDelete);
            tvDeleteText = itemView.findViewById(R.id.tvDeleteText);
        }
    }


    private String formatRupiah(int value) {
        return String.format(Locale.US, "%,d", value).replace(",", ".");
    }
}
