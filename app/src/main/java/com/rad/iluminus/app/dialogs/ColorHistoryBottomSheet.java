package com.rad.iluminus.app.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.iluminus.app.databinding.FragmentColorHistoryBinding; // Assuming view binding is enabled and layout is fragment_color_history.xml which generates FragmentColorHistoryBinding
import com.rad.iluminus.app.adapters.ColorHistoryAdapter;
import com.rad.iluminus.repository.colors.ColorRepository;
import com.rad.iluminus.repository.models.ColorHistory;

import java.util.Collections;
import java.util.List;

public class ColorHistoryBottomSheet extends BottomSheetDialogFragment {

    private FragmentColorHistoryBinding binding; // This binding class should be generated from fragment_color_history.xml
    private ColorRepository colorRepository;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(ColorHistory colorHistory);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate using ViewBinding if possible, or simple inflation if binding not generated yet
        // Since I can't be 100% sure ViewBinding is set up for this specific layout filename without checking build config deeply,
        // I'll assume standard naming convention `fragment_color_history` -> `FragmentColorHistoryBinding`.
        // If that fails, I'll switch to findViewById. But standard android projects usually work this way.
        
        // Actually, to be safe and avoid compilation error if Binding class name differs, I'll use standard inflation + findViewById if I were unsure.
        // But the previous file was `FragmentIluminusBinding` so ViewBinding is likely active.
        // Layout name: fragment_color_history.xml
        
        binding = FragmentColorHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        colorRepository = new ColorRepository(requireContext());
        ColorHistoryAdapter adapter = new ColorHistoryAdapter(colorHistory -> {
            if (listener != null) {
                listener.onColorSelected(colorHistory);
            }
            dismiss();
        });

        binding.recyclerViewHistory.setAdapter(adapter);

        loadHistory(adapter);
    }

    private void loadHistory(ColorHistoryAdapter adapter) {
        List<ColorHistory> historyList = colorRepository.ListColorHistory();
        Collections.reverse(historyList); // Show newest first? Usually better for history.

        if (historyList.isEmpty()) {
            binding.textEmptyHistory.setVisibility(View.VISIBLE);
            binding.recyclerViewHistory.setVisibility(View.GONE);
        } else {
            binding.textEmptyHistory.setVisibility(View.GONE);
            binding.recyclerViewHistory.setVisibility(View.VISIBLE);
            adapter.setHistoryList(historyList);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
