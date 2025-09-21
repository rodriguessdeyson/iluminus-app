package com.rad.iluminus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.rad.iluminus.databinding.FragmentMainBinding;

public class FragmentMain extends Fragment {

    private FragmentMainBinding binding;

@Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        binding = FragmentMainBinding.inflate(inflater, container, false);

        checkPermission();
        
        binding.IncludeSetup.ButtonConfigure.setOnClickListener(v ->
        {
            NavHostFragment.findNavController(FragmentMain.this)
                    .navigate(R.id.action_FragmentMain_to_FragmentPermission);
        });

        binding.IncludeInit.ButtonSearchDevice.setOnClickListener(v ->
        {
            NavHostFragment.findNavController(FragmentMain.this)
                    .navigate(R.id.action_FragmentMain_to_FragmentIluminus);
        });

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void checkPermission() {
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
        else
        {
            permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };
        }

        if (arePermissionsGranted(permissions))
        {
            binding.IncludeSetup.MaterialCardViewSetup.setVisibility(View.GONE);
            binding.IncludeInit.MaterialCardViewInit.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.IncludeSetup.MaterialCardViewSetup.setVisibility(View.VISIBLE);
            binding.IncludeInit.MaterialCardViewInit.setVisibility(View.GONE);
        }
    }

    private boolean arePermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false; // At least one permission is not granted
            }
        }
        return true; // All permissions are granted
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }
}