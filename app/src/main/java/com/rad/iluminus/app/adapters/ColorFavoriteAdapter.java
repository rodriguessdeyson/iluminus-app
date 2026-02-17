package com.rad.iluminus.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iluminus.app.R;
import com.rad.iluminus.repository.models.ColorFavorite;

import java.util.ArrayList;
import java.util.List;

public class ColorFavoriteAdapter extends RecyclerView.Adapter<ColorFavoriteAdapter.ViewHolder> {

    private List<ColorFavorite> favoriteList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ColorFavorite colorFavorite);
        void onDeleteClick(ColorFavorite colorFavorite);
    }

    public ColorFavoriteAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFavoriteList(List<ColorFavorite> favoriteList) {
        this.favoriteList = favoriteList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ColorFavorite favorite = favoriteList.get(position);
        holder.bind(favorite, listener);
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View viewColor;
        private final TextView textRGB;
        private final TextView textDate;
        private final ImageButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.viewColor);
            textRGB = itemView.findViewById(R.id.textRGB);
            textDate = itemView.findViewById(R.id.textDate);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(final ColorFavorite favorite, final OnItemClickListener listener) {
            viewColor.setBackgroundColor(favorite.GetColor());
            textRGB.setText(String.format("R:%d G:%d B:%d", favorite.getR(), favorite.getG(), favorite.getB()));
            textDate.setText(favorite.getSavedTime());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(favorite);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(favorite);
                }
            });
        }
    }
}
