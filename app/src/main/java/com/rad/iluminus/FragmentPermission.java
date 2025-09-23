package com.rad.iluminus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rad.iluminus.databinding.FragmentPermissionBinding;

public class FragmentPermission extends Fragment {

    private FragmentPermissionBinding binding;
    private ActivityResultLauncher<String[]> bluetoothLauncher;
    private ActivityResultLauncher<String> locationLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        bluetoothLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            isGranted ->
            {
                if (Boolean.TRUE.equals(isGranted.get(Manifest.permission.BLUETOOTH_CONNECT)) &&
                    Boolean.TRUE.equals(isGranted.get(Manifest.permission.BLUETOOTH_SCAN)))
                {
                    binding.animationAllowBluetoothView.setRepeatCount(0);
                    binding.animationAllowBluetoothView.setAnimation("lottie_enabled.json");
                    binding.animationAllowBluetoothView.playAnimation();
                    binding.ButtonAllowBluetooth.setEnabled(false);

                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() ->
                    {
                        binding.MaterialCardViewBluetoothPermission.setVisibility(View.GONE);
                        binding.MaterialCardViewLocationPermission.setVisibility(View.VISIBLE);
                    }, 3000);
                }
                else
                {
                    showBluetoothPermissionRationale();
                }
            });

        locationLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            isGranted ->
            {
                if (Boolean.TRUE.equals(isGranted))
                {
                    binding.animationAllowLocationView.setRepeatCount(0);
                    binding.animationAllowLocationView.setAnimation("lottie_enabled.json");
                    binding.animationAllowLocationView.playAnimation();
                    binding.ButtonAllowLocation.setEnabled(false);

                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() ->
                    {
                        // Vai para o fragmento de pesquisa de dispositivos.
                    }, 3000);
                }
                else
                {
                    showLocationPermissionRationale();
                }
            });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        binding = FragmentPermissionBinding.inflate(inflater, container, false);

        binding.ButtonAllowBluetooth.setOnClickListener(v -> requestBluetoothPermission());

        binding.ButtonAllowLocation.setOnClickListener(v -> requestLocationPermission());

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

    private String arePermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED)
            {
                return permission; // At least one permission is not granted
            }
        }
        return null; // All permissions are granted
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBluetoothPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            bluetoothLauncher.launch(new String[] {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH
            });
        }
    }

    private void requestLocationPermission()
    {
        locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void showBluetoothPermissionRationale()
    {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder
            .setTitle("Permissions Required")
            .setMessage("Bluetooth permission is required for this feature.")
            .setPositiveButton("Grant Permissions", (dialog, id) -> this.requestBluetoothPermission())
            .setNegativeButton("Não permitir", (dialog, id) ->
                {
                    Toast.makeText(requireContext(), "Permissão negada. Aplicação será fechada.", Toast.LENGTH_SHORT).show();
                    this.requireActivity().finish();
                });

        // Create the AlertDialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLocationPermissionRationale()
    {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder
                .setTitle("Permissions Required")
                .setMessage("Location permission is required for this feature.")
                .setPositiveButton("Grant Permissions", (dialog, id) -> this.requestLocationPermission())
                .setNegativeButton("Não permitir", (dialog, id) ->
                {
                    Toast.makeText(requireContext(), "Permissão negada. Aplicação será fechada.", Toast.LENGTH_SHORT).show();
                    this.requireActivity().finish();
                });

        // Create the AlertDialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}