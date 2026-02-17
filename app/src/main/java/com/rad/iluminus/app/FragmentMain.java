package com.rad.iluminus.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.iluminus.app.R;
import com.iluminus.app.databinding.FragmentMainBinding;
import com.rad.iluminus.network.HttpService;

public class FragmentMain extends Fragment {

    private FragmentMainBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        binding = FragmentMainBinding.inflate(inflater, container, false);

        binding.IncludeSetup.MaterialCardViewSetup.setVisibility(View.GONE);
        binding.IncludeInit.MaterialCardViewInit.setVisibility(View.VISIBLE);
        
        binding.IncludeSetup.ButtonConfigure.setOnClickListener(v ->
        {
            NavHostFragment.findNavController(FragmentMain.this)
                    .navigate(R.id.action_FragmentMain_to_FragmentConfiguration);
        });

        binding.IncludeInit.ButtonSearchDevice.setOnClickListener(v ->
        {
            NavHostFragment.findNavController(FragmentMain.this)
                    .navigate(R.id.action_FragmentMain_to_FragmentConfiguration);
        });

        checkSavedIpAndConnect();

        return binding.getRoot();
    }

    private void checkSavedIpAndConnect() {
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("IluminusPrefs", android.content.Context.MODE_PRIVATE);
        String savedIp = prefs.getString("device_ip", null);

        if (savedIp != null && !savedIp.isEmpty()) {
            binding.IncludeInit.ButtonSearchDevice.setVisibility(View.GONE);
            binding.IncludeInit.LinearLayoutLoading.setVisibility(View.VISIBLE);
            binding.IncludeInit.TextViewLoadingStatus.setText("Conectando ao " + savedIp + "...");
            binding.IncludeInit.TextViewHeaderSubtitle.setText("Dispositivo localizado");

            HttpService.sendCommand(savedIp, "CHECK", new HttpService.Callback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        binding.IncludeInit.TextViewLoadingStatus.setText("Conectado!");
                        NavHostFragment.findNavController(FragmentMain.this)
                                .navigate(R.id.action_FragmentMain_to_FragmentIluminus);
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (isAdded()) {
                        binding.IncludeInit.LinearLayoutLoading.setVisibility(View.GONE);
                        binding.IncludeInit.ButtonSearchDevice.setVisibility(View.VISIBLE);
                        binding.IncludeInit.TextViewHeaderSubtitle.setText("Falha na conexão. Verifique o ip e se o dispositivo está energizado");
                        android.widget.Toast.makeText(requireContext(), "Falha ao conectar ao dispositivo configurado", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            binding.IncludeInit.ButtonSearchDevice.setVisibility(View.VISIBLE);
            binding.IncludeInit.LinearLayoutLoading.setVisibility(View.GONE);
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}