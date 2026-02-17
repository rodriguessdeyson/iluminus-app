package com.rad.iluminus.app.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.iluminus.app.databinding.FragmentColorFavoriteBinding;
import com.rad.iluminus.app.adapters.ColorFavoriteAdapter;
import com.rad.iluminus.repository.colors.ColorRepository;
import com.rad.iluminus.repository.models.ColorFavorite;

import java.util.Collections;
import java.util.List;

public class ColorFavoriteBottomSheet extends BottomSheetDialogFragment {

    private FragmentColorFavoriteBinding binding;
    private ColorRepository colorRepository;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(ColorFavorite colorFavorite);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentColorFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        colorRepository = new ColorRepository(requireContext());
        ColorFavoriteAdapter adapter = new ColorFavoriteAdapter(new ColorFavoriteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ColorFavorite colorFavorite) {
                if (listener != null) {
                    listener.onColorSelected(colorFavorite);
                }
                dismiss();
            }

            @Override
            public void onDeleteClick(ColorFavorite colorFavorite) {
                colorRepository.DeleteFavorite(colorFavorite.getId());
                Toast.makeText(requireContext(), "Cor excluída", Toast.LENGTH_SHORT).show();
                loadFavorites();
            }
        });

        binding.recyclerViewFavorites.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        if (binding == null) return;
        
        List<ColorFavorite> favoriteList = colorRepository.ListFavorites();
        Collections.reverse(favoriteList);

        ColorFavoriteAdapter adapter = (ColorFavoriteAdapter) binding.recyclerViewFavorites.getAdapter();
        if (adapter != null) {
            adapter.setFavoriteList(favoriteList);
        }

        if (favoriteList.isEmpty()) {
            binding.textEmptyFavorites.setVisibility(View.VISIBLE);
            binding.recyclerViewFavorites.setVisibility(View.GONE);
        } else {
            binding.textEmptyFavorites.setVisibility(View.GONE);
            binding.recyclerViewFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
