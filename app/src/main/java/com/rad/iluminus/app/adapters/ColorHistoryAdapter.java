package com.rad.iluminus.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iluminus.app.R;
import com.rad.iluminus.repository.models.ColorHistory;

import java.util.ArrayList;
import java.util.List;

public class ColorHistoryAdapter extends RecyclerView.Adapter<ColorHistoryAdapter.ViewHolder> {

    private List<ColorHistory> historyList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ColorHistory colorHistory);
    }

    public ColorHistoryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setHistoryList(List<ColorHistory> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ColorHistory history = historyList.get(position);
        holder.bind(history, listener);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View viewColor;
        private final TextView textDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.viewColor);
            textDate = itemView.findViewById(R.id.textDate);
        }

        public void bind(final ColorHistory history, final OnItemClickListener listener) {
            viewColor.setBackgroundColor(history.GetColor());
            textDate.setText(history.getSavedTime());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(history);
                }
            });
        }
    }
}
