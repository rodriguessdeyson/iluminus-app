package com.rad.iluminus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rad.Bluetooth.BluetoothHandler;
import com.rad.Database.DatabaseAccess;
import com.rad.Database.models.BluetoothDevicesModel;
import com.rad.Utils.Alert;
import com.rad.Utils.Lumos;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED;
import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;
import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothDevice.ACTION_FOUND;
import static android.bluetooth.BluetoothDevice.ACTION_UUID;
import static android.os.Build.VERSION.SDK_INT;

public class BluetoothConnection extends AppCompatActivity
{
	//region Attributes

	/**
	 * Allows to manipulate the Bluetooth adapter.
	 */
	private BluetoothAdapter bluetoothAdapter;

	/**
	 * Flag to handle activity request.
	 */
	private static final int ActivityRequestCode = 1;

	/**
	 * Flag to handle permissions request.
	 */
	private static final int PermissionsRequestCode = 2;

	/**
	 * Flag to indicate if all process has executed.
	 */
	private int HasExecutedAll = 0;

	/**
	 * Object to manipulate database access.
	 */
	private DatabaseAccess IDatabase;

	/**
	 * Object to manipulate Bluetooth device.
	 */
	private final BluetoothHandler Bluetooth = new BluetoothHandler();

	/**
	 * Object to manipulate an progress indicator.
	 */
	private Alert ProgressSearch;

	/**
	 * Object adapter for list view to shows bluetooth devices.
	 */
	private ArrayAdapter<BluetoothDevicesModel> ListAdapter;

	/**
	 * List with all Bluetooth devices located.
	 */
	private final ArrayList<BluetoothDevicesModel> BModels = new ArrayList<>();

	/**
	 * Intent filter to filter/notify actions.
	 */
	private IntentFilter Filter;

	private ActivityResultLauncher<Intent> enableBluetoothLauncher;

	//endregion

	//region Views

	/**
	 * Floating button scan reference.
	 */
	private FloatingActionButton FloatingActionScan;

	//endregion

	//region Load Overrides.

	/**
	 * Method called when application started
	 * @param savedInstanceState Current saved instance
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Set screens to all configuration.
		SetScreenSettings();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_configure_bluetoothdevice);

		bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();

		enableBluetoothLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), result ->
			{
				if (result.getResultCode() == Activity.RESULT_OK)
				{
					// Bluetooth enabled successfully
					Toast.makeText(this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
				}
				else if (result.getResultCode() == Activity.RESULT_CANCELED)
				{
					// User canceled enabling Bluetooth
					Toast.makeText(this, "Bluetooth Not Enabled", Toast.LENGTH_SHORT).show();
				}
				else
				{
					// User denied Bluetooth enabling
					Toast.makeText(this, "Bluetooth Not Enabled", Toast.LENGTH_SHORT).show();
				}
			}
		);

		checkAndEnableBluetooth();

		// Initialize all controls.
		StartViewContents();

		// Initialize filters.
		FilteringActions();

		// Method to treat all events.
		ControlsEvent();
	}

	/**
	 * Method called when the application restart from tray.
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
	}


	/**
	 * Method called when the application goes to pause mode.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	/**
	 * Method called when the application is destroyed.
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Bluetooth.TurnOff();
	}

	//endregion

	//region Method

	private void checkAndEnableBluetooth()
	{
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter == null)
		{
			// Device doesn't support Bluetooth
			Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!bluetoothAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			enableBluetoothLauncher.launch(enableBtIntent);
		}
		else
		{
			// Bluetooth already enabled
			Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Method to configure users app interface.
	 */
	@SuppressLint("SourceLockedOrientationActivity")
	private void SetScreenSettings()
	{
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * Method to initiate all view controllers.
	 */
	private void StartViewContents()
	{
		FloatingActionScan  = findViewById(R.id.FloatingActionButtonScan);
		IDatabase           = new DatabaseAccess(this, getString(R.string.DatabaseName));
		Filter              = new IntentFilter();
		ListAdapter         = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, 0);
		ProgressSearch      = new Alert(this, Alert.ProgressType.SEARCHING_PROGRESS);
		ListView listView   = findViewById(R.id.ListViewBluetoothDevices);
		if (listView != null)
		{
			listView.setAdapter(ListAdapter);
			listView.setOnItemClickListener(OnDevicesListClickListener);
		}
	}

	/**
	 * 	Method to filter Bluetooth actions.
	 */
	private void FilteringActions()
	{
		Filter.addAction(ACTION_FOUND);
		Filter.addAction(ACTION_DISCOVERY_STARTED);
		Filter.addAction(ACTION_DISCOVERY_FINISHED);
		Filter.addAction(ACTION_STATE_CHANGED);
		Filter.addAction(ACTION_UUID);
	}

	/**
	 * Manipulate all events.
	 */
	private void ControlsEvent()
	{
		/*
		  Event triggered when the Start button is pressed.
		 */
		FloatingActionScan.setOnClickListener(v ->
		{
			ListAdapter.clear();
		});
	}

	/**
	 * Listener to handle ListView clicks.
	 */
	private final AdapterView.OnItemClickListener OnDevicesListClickListener =
		new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position, long id)
			{
				// Get the selected device address.
				BluetoothDevicesModel device = (BluetoothDevicesModel)av
					.getAdapter()
					.getItem(position);

				// Checks if the device is in database.
				List<BluetoothDevicesModel> devices = IDatabase.ReadBluetoothConfiguration();

				// If the database contains no device, insert the select one.
				// Else, checks if the device is already in database.
				if (devices.isEmpty())
					IDatabase.Insert(device);
				else
				{
					boolean isSaved = false;
					for (BluetoothDevicesModel dv : devices)
					{
						if (dv.getDeviceAddress().equals(device.getDeviceAddress()))
						{
							isSaved = true;
							break;
						}
					}
					if (!isSaved)
						IDatabase.Insert(device);
				}

				// Pass through intent to the next activity connections information.
				Intent iluminus = new Intent(BluetoothConnection.this,
					IluminusBluetoothActivity.class);
				startActivity(iluminus);
			}
		};

	/**
	 * BroadcastReceiver to capture all events filtered of bluetooth actions.
	 */
	BroadcastReceiver bluetoothReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Retrieves the intent information.
			String action = intent.getAction();
			if (action == null)
				return;

			// Checks which action was triggered.
			BluetoothDevicesModel deviceModel;
			switch (action)
			{
				case ACTION_DISCOVERY_STARTED:
					ProgressSearch.Show();
					break;

				case ACTION_FOUND:
					deviceModel = Bluetooth.GetBluetoothDevice(intent);
					if (deviceModel != null && deviceModel.getDeviceName() != null)
						BModels.add(deviceModel);
					break;

				case ACTION_DISCOVERY_FINISHED:
					if (BModels.isEmpty())
					{
						Lumos.Show(getApplicationContext(), R.string.Main_No_Device_Found);
						ProgressSearch.Dismiss();
					}
					else
					{
						for (int i = 0; i < BModels.size(); i++)
							BModels.get(i).getDevice().fetchUuidsWithSdp();
					}
					break;

				case ACTION_STATE_CHANGED:
					if (Bluetooth.GetBluetoothState() == STATE_OFF)
						startActivityForResult(Bluetooth.TurnOn(), ActivityRequestCode);
					break;

				case ACTION_UUID:
					HasExecutedAll += 1;
					for (BluetoothDevicesModel m : BModels)
					{
						String bAddress = Bluetooth.GetBluetoothDevice(intent).getDeviceAddress();
						if (m.getDeviceAddress().equals(bAddress))
						{
							if (Bluetooth.GetUUID(intent) == Bluetooth.DefaultUUID)
							{
								// Removes the device and continue iterating.
								BModels.remove(m);
								continue;
							}
							m.setServiceUUID(Bluetooth.GetUUID(intent));
						}
					}
					if (ListAdapter.getCount() == 0 && HasExecutedAll == BModels.size())
					{
						ListAdapter.addAll(BModels);
						ProgressSearch.Dismiss();
						HasExecutedAll = 0;
					}
					break;
				default:
					throw new UnsupportedOperationException("Invalid action" + action);
			}
		}
	};


	//endregion
}
