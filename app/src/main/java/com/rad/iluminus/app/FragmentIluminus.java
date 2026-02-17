package com.rad.iluminus.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.iluminus.app.R;
import com.rad.iluminus.app.dialogs.ColorFavoriteBottomSheet;
import com.rad.iluminus.app.dialogs.ColorHistoryBottomSheet;
import com.rad.iluminus.repository.colors.ColorRepository;
import com.rad.iluminus.repository.models.ColorHistory;
import android.os.VibrationEffect;
import android.os.Vibrator;
import com.rad.iluminus.repository.models.ColorFavorite;
import com.iluminus.app.databinding.FragmentIluminusBinding;
import com.rad.iluminus.network.HttpService;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

public class FragmentIluminus extends Fragment {

    private FragmentIluminusBinding binding;
    private SharedPreferences sharedPreferences;
    private String deviceIp;
    private ColorRepository colorRepository;
    private boolean isPowerOn = true;
    
    // Debouncing
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingCommand = null;
    private static final long DEBOUNCE_DELAY_MS = 100;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentIluminusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        AppBarLayout appBarLayout = requireActivity().findViewById(R.id.app_bar);
        if (appBarLayout != null) {
            appBarLayout.setExpanded(false, false);
        }

        sharedPreferences = requireActivity().getSharedPreferences("IluminusPrefs", Context.MODE_PRIVATE);
        deviceIp = sharedPreferences.getString("device_ip", null);

        if (deviceIp == null) {
            Toast.makeText(requireContext(), "Antes de começar, configure o IP do dispositivo", Toast.LENGTH_LONG).show();
            return;
        }

        colorRepository = new ColorRepository(requireContext());

        setupColorPicker();
        setupButtons();
    }

    private void setupColorPicker() {
        binding.ColorPickerView.attachBrightnessSlider(binding.BrightnessSlide);

        binding.ColorPickerView.setColorListener((ColorEnvelopeListener) (envelope, fromUser) -> {
            if (fromUser) {
                int color = envelope.getColor();
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                
                binding.TextViewParametersSubTitle.setText(String.format("R:%d G:%d B:%d", r, g, b));
            }
        });
    }

    private void setupButtons() {
        binding.ButtonReset.setOnClickListener(v -> {
            isPowerOn = !isPowerOn;
            String cmd = isPowerOn ? "L" : "D"; 
            
            if (isPowerOn) {
                 int color = binding.ColorPickerView.getColor();
                 int r = Color.red(color);
                 int g = Color.green(color);
                 int b = Color.blue(color);
                 cmd = "L&r=" + r + "&g=" + g + "&b=" + b;
                 Toast.makeText(getContext(), "Power ON", Toast.LENGTH_SHORT).show();
            } else {
                 Toast.makeText(getContext(), "Power OFF", Toast.LENGTH_SHORT).show();
            }
            
            scheduleCommand(cmd);
        });

        binding.ButtonPreviousColor.setOnClickListener(v -> {
             ColorHistoryBottomSheet bottomSheet = new ColorHistoryBottomSheet();
             bottomSheet.setOnColorSelectedListener(history -> {
                 int r = history.getR();
                 int g = history.getG();
                 int b = history.getB();

                 String cmd = "L&r=" + r + "&g=" + g + "&b=" + b;
                 scheduleCommand(cmd);

                 if (binding.TextViewParametersSubTitle != null) {
                     binding.TextViewParametersSubTitle.setText(String.format("R:%d G:%d B:%d", r, g, b));
                 }
                 Toast.makeText(getContext(), "Color Resent", Toast.LENGTH_SHORT).show();
             });
             bottomSheet.show(getParentFragmentManager(), "ColorHistoryBottomSheet");
        });

        binding.ButtonSendColor.setOnClickListener(v -> {
                int color = binding.ColorPickerView.getColor();
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                
                String cmd = "L&r=" + r + "&g=" + g + "&b=" + b;
                scheduleCommand(cmd);

                ColorHistory history = new ColorHistory();
                history.setA(Color.alpha(color));
                history.setR(r);
                history.setG(g);
                history.setB(b);
                colorRepository.Insert(history);

                Toast.makeText(getContext(), "Color Sent & Saved", Toast.LENGTH_SHORT).show();
        });

        binding.ButtonSendColor.setOnLongClickListener(v -> {
            int color = binding.ColorPickerView.getColor();
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            ColorFavorite favorite = new ColorFavorite();
            favorite.setA(Color.alpha(color));
            favorite.setR(r);
            favorite.setG(g);
            favorite.setB(b);
            colorRepository.InsertFavorite(favorite);

            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                     vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                 } else {
                     vibrator.vibrate(100);
                 }
            }

            String cmd = "L&r=" + r + "&g=" + g + "&b=" + b;
            scheduleCommand(cmd);

            Toast.makeText(getContext(), "Cor adicionada aos favoritos", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Favorite Button
        binding.ButtonFavorite.setOnClickListener(v -> {
             ColorFavoriteBottomSheet bottomSheet = new ColorFavoriteBottomSheet();
             bottomSheet.setOnColorSelectedListener(favorite -> {
                 int r = favorite.getR();
                 int g = favorite.getG();
                 int b = favorite.getB();

                 String cmd = "L&r=" + r + "&g=" + g + "&b=" + b;
                 scheduleCommand(cmd);

                 if (binding.TextViewParametersSubTitle != null) {
                     binding.TextViewParametersSubTitle.setText(String.format("R:%d G:%d B:%d", r, g, b));
                 }
                 Toast.makeText(getContext(), "Cor enviada!", Toast.LENGTH_SHORT).show();
             });
             bottomSheet.show(getParentFragmentManager(), "ColorFavoriteBottomSheet");
        });
    }

    private void scheduleCommand(String commandParams) {
        if (pendingCommand != null) {
            debounceHandler.removeCallbacks(pendingCommand);
        }

        pendingCommand = () -> {
            HttpService.sendCommand(deviceIp, commandParams, null); 
        };

        debounceHandler.postDelayed(pendingCommand, DEBOUNCE_DELAY_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}