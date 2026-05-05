package com.rad.iluminus.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.iluminus.app.R;
import com.iluminus.app.databinding.FragmentConfigurationBinding;
import com.rad.iluminus.network.HttpService;
import com.rad.iluminus.network.DiscoveryService;

public class FragmentConfiguration extends Fragment {

    private FragmentConfigurationBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "IluminusPrefs";
    private static final String KEY_IP_ADDRESS = "device_ip";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConfigurationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedIp = sharedPreferences.getString(KEY_IP_ADDRESS, "192.168.4.1");
        binding.etIpAddress.setText(savedIp);

        binding.btnScanNetwork.setOnClickListener(v -> {
            binding.tvStatus.setText("Procurando ilumins...");
            binding.btnScanNetwork.setEnabled(false);
            
            DiscoveryService.scanNetwork(new DiscoveryService.DiscoveryCallback() {
                @Override
                public void onDeviceFound(String ipAddress) {
                    if (isAdded()) {
                         binding.etIpAddress.setText(ipAddress);
                         binding.tvStatus.setText("Dispositivo localizado: " + ipAddress);
                         binding.btnScanNetwork.setEnabled(true);
                         Toast.makeText(requireContext(), "Dispositivo localizado!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                     if (isAdded()) {
                         binding.tvStatus.setText("Falha ao realizar a pesquisa: " + error);
                         binding.btnScanNetwork.setEnabled(true);
                     }
                }
            });
        });

        binding.btnSaveAndConnect.setOnClickListener(v -> {
            String ip = binding.etIpAddress.getText().toString().trim();
            if (ip.isEmpty()) {
                binding.etIpAddress.setError("Endereço IP do dispositivo é necessário");
                return;
            }

            binding.tvStatus.setText("Status: Conectando...");
            binding.btnSaveAndConnect.setEnabled(false);

            HttpService.sendCommand(ip, "CHECK", new HttpService.Callback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        binding.tvStatus.setText("Status: Conectado!");
                        binding.btnSaveAndConnect.setEnabled(true);
                        
                        // Save IP
                        sharedPreferences.edit().putString(KEY_IP_ADDRESS, ip).apply();
                        Toast.makeText(requireContext(), "Conectado...", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to control screen
                        NavHostFragment.findNavController(FragmentConfiguration.this)
                                .navigate(R.id.action_FragmentConfiguration_to_FragmentIluminus);
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (isAdded()) {
                        binding.tvStatus.setText("Status: Falha na conexão");
                        binding.btnSaveAndConnect.setEnabled(true);
                        Toast.makeText(requireContext(), "Falha na conexão: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
