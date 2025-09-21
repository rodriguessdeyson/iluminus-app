package com.rad.iluminus;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothDevice.ACTION_FOUND;
import static android.bluetooth.BluetoothDevice.ACTION_UUID;
import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rad.adapters.BluetoothDeviceListViewAdapter;
import com.rad.iluminus.databinding.FragmentSearchDeviceBinding;
import com.rad.listeners.BluetoothSelectItemListener;
import com.rad.models.BluetoothDeviceModel;

import java.util.ArrayList;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class FragmentSearchDevice extends Fragment implements BluetoothSelectItemListener
{
    /**
     * Default UUID returned if the desired one is not found.
     */
    public final UUID DefaultUUID = UUID.fromString("00000000-0000-0000-0000-00800000000");

    /**
     * Flag to indicate if all process has executed.
     */
    private int HasExecutedAll = 0;

    private FragmentSearchDeviceBinding binding;
    
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceListViewAdapter deviceListAdapter;
    private final ArrayList<BluetoothDeviceModel> bluetoothAvailableDeviceModels = new ArrayList<>();
    private final ArrayList<BluetoothDeviceModel> bluetoothDeviceModels = new ArrayList<>();
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private FloatingActionButton fab;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    
        fab = requireActivity().findViewById(R.id.FABSearchDevices);
        fab.setOnClickListener(view -> {
            deviceListAdapter.clear();
            bluetoothAvailableDeviceModels.clear();
            bluetoothDeviceModels.clear();
            HasExecutedAll = 0;
            performDiscovery();
        });
    
        enableBluetoothLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result ->
            {
                if (result.getResultCode() == Activity.RESULT_OK)
                    Toast.makeText(requireActivity(), "Bluetooth enabled successfully", Toast.LENGTH_SHORT).show();
                else
                {
                    showBluetoothTurnOnRationale();
                    Toast.makeText(requireActivity(), "Bluetooth enabling denied", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            cancelDiscovery();
        }
    };

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        binding = FragmentSearchDeviceBinding.inflate(inflater, container, false);
        
        deviceListAdapter = new BluetoothDeviceListViewAdapter(requireContext(), bluetoothDeviceModels);
        deviceListAdapter.setBluetoothSelectItemListener(this);
        deviceListAdapter.notifyDataSetChanged();
		ListView listView = binding.ListViewBluetoothDevices;
        listView.setAdapter(deviceListAdapter);
        
        // Add an AnimatorListener to reverse direction on animation end
        binding.animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }
            
            @Override
            public void onAnimationEnd(Animator animation) { }
            
            @Override
            public void onAnimationCancel(Animator animation) { }
            
            @Override
            public void onAnimationRepeat(Animator animation) {
                binding.animationView.setSpeed(binding.animationView.getSpeed() * -1);
                binding.animationView.playAnimation();
            }
        });
        
        performDiscovery();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    private void performDiscovery()
    {
        this.cancelDiscovery();
    
        Toast.makeText(requireContext(), "Starting Discovery...", Toast.LENGTH_SHORT).show();
    
        // Register BroadcastReceiver for ACTION_FOUND and ACTION_DISCOVERY_FINISHED
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DISCOVERY_STARTED);
        filter.addAction(ACTION_UUID);
        filter.addAction(ACTION_FOUND);
        filter.addAction(ACTION_DISCOVERY_FINISHED);
        requireContext().registerReceiver(bluetoothReceiver, filter);
    
        // Start discovery
        bluetoothAdapter.startDiscovery();
    }

    private void cancelDiscovery()
    {
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    private void showBluetoothTurnOnRationale()
    {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder
            .setTitle("Bluetooth Enabled Required")
            .setMessage("Bluetooth need to be enabled for this feature.")
            .setPositiveButton("Grant Permissions", (dialog, id) -> this.requestBluetoothEnable())
            .setNegativeButton("Não permitir", (dialog, id) ->
            {
                Toast.makeText(requireActivity(), "Permissão negada. Aplicação será fechada.", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            });
        
        // Create the AlertDialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void requestBluetoothEnable()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothLauncher.launch(enableBtIntent);
    }

    //#region Bluetooth Broadcast

    /**
     * Bluetooth Broadcast Receiver.
     */
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver()
    {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Retrieves the intent information.
            String action = intent.getAction();
            if (action == null)
                return;
            
            switch (action)
            {
                case ACTION_DISCOVERY_STARTED:
                    processDiscovery();
                    break;
                
                case ACTION_FOUND:
                    registerDeviceFound(intent);
                    break;
                
                case ACTION_DISCOVERY_FINISHED:
                    processDiscoveredDevices();
                    break;
                
                case ACTION_STATE_CHANGED:
                    if (bluetoothAdapter.getState() == STATE_OFF)
                        requestBluetoothEnable();
                    break;
                
                case ACTION_UUID:
                    registerDeviceUuidDiscovery(intent);
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid action" + action);
            }
        }
    };

    private void processDiscovery()
    {
        fab.setEnabled(false);
        fab.setClickable(false);
        binding.TextViewHeaderTitle.setText("Pesquisando dispositivos...");
        binding.animationView.setVisibility(View.VISIBLE);
        binding.ListViewBluetoothDevices.setVisibility(View.GONE);
    }

/**
     * Registers the device found.
     * @param intent Operation to be performed.
     */
    private void registerDeviceFound(Intent intent) {
        BluetoothDevice deviceFound = intent.getParcelableExtra(EXTRA_DEVICE);
        
        if (deviceFound != null && deviceFound.getName() != null)
        {
            BluetoothDeviceModel bt = new BluetoothDeviceModel(deviceFound.getName(), deviceFound.getAddress(), deviceFound);
            bluetoothAvailableDeviceModels.add(bt);
        }
    }

    /**
     * Processes the discovered devices.
     */
    private void processDiscoveredDevices()
    {
        fab.setEnabled(true);
        fab.setClickable(true);
        
        if (bluetoothAvailableDeviceModels.isEmpty())
            binding.TextViewHeaderTitle.setText("Nenhum dispositivo encontrado");
        else
        {
            binding.TextViewHeaderTitle.setText("Recuperando informações dos dispositivos...");
            for (int i = 0; i < bluetoothAvailableDeviceModels.size(); i++)
                bluetoothAvailableDeviceModels.get(i).getDevice().fetchUuidsWithSdp();
        }
    }

    /**
     * Registers the devices UUID.
     * @param intent Operation to be performed.
     */
    private void registerDeviceUuidDiscovery(Intent intent) {
            HasExecutedAll += 1;
            BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
            
            if (device == null)
                return;
            
            String deviceAddress = device.getAddress();
            
            for (BluetoothDeviceModel m : bluetoothAvailableDeviceModels)
            {
                if (m.getAddress().equals(deviceAddress) && !bluetoothDeviceModels.contains(m))
                {
                    UUID uuid = getUUID(intent);
                    if (uuid == DefaultUUID) continue;
                    m.setServiceUUID(uuid);
                    bluetoothDeviceModels.add(m);
                }
            }
            
            if (deviceListAdapter.getCount() == 1 && HasExecutedAll == bluetoothAvailableDeviceModels.size())
            {
                deviceListAdapter.notifyDataSetChanged();
                HasExecutedAll = 0;
                
                binding.TextViewHeaderTitle.setText("Dispositivos encontrados");
                binding.animationView.setVisibility(View.GONE);
                binding.ListViewBluetoothDevices.setVisibility(View.VISIBLE);
                cancelDiscovery();
            }
        }

    /**
     * Gets the devices UUID discovered.
     * @param intent The intent of the request.
     * @return The UUID required.
     */
    public UUID getUUID(Intent intent)
    {
        Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
        UUID desiredUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        
        if (uuidExtra == null)
            return DefaultUUID;
        
        String uuidService = DefaultUUID.toString();
        for (Parcelable uuid : uuidExtra)
        {
            // Checks if the service is available in device.
            if (!uuid.toString().equals(desiredUUID.toString()))
                return DefaultUUID;
            
            // Saves the uuid id.
            uuidService = uuid.toString();
            break;
        }
        return UUID.fromString(uuidService);
    }

    @Override
    public void onConnectClick(BluetoothDeviceModel bluetoothDeviceModel)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("bluetoothDeviceModel", bluetoothDeviceModel);

        NavHostFragment.findNavController(FragmentSearchDevice.this)
            .navigate(R.id.action_FragmentSearchDevice_to_FragmentIluminus, bundle);
    }

//#endregion
}