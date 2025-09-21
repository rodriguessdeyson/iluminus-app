package com.rad.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rad.iluminus.R;
import com.rad.listeners.BluetoothSelectItemListener;
import com.rad.models.BluetoothDeviceModel;

import java.util.ArrayList;

public class BluetoothDeviceListViewAdapter extends ArrayAdapter<BluetoothDeviceModel>
{
	private final ArrayList<BluetoothDeviceModel> bluetoothDevices;
	private BluetoothSelectItemListener bluetoothSelectItemListener;

	public BluetoothDeviceListViewAdapter(Context context, ArrayList<BluetoothDeviceModel> resistorHistory)
	{
		super(context, android.R.layout.simple_list_item_1, resistorHistory);
		this.bluetoothDevices = resistorHistory;
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
	{
		View row;
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.layout_bluetooth_device_item_component, parent, false);
		}
		else
			row = convertView;
		
		BluetoothDeviceModel resistor = bluetoothDevices.get(position);
		setDeviceName(row, resistor);
		setDeviceUUID(row, resistor);
		setDeviceAddress(row, resistor);
		setConnectButton(row, resistor);
		return row;
	}
	
	public void setBluetoothSelectItemListener(BluetoothSelectItemListener bluetoothSelectItemListener)
	{
		this.bluetoothSelectItemListener = bluetoothSelectItemListener;
	}

	private void setDeviceName(View row, BluetoothDeviceModel resistor)
	{
		TextView deviceName = row.findViewById(R.id.TextViewDeviceName);
		deviceName.setText(resistor.getName());
	}

	private void setDeviceUUID(View row, BluetoothDeviceModel resistor)
	{
		TextView deviceUUID = row.findViewById(R.id.TextViewDeviceUUID);
		deviceUUID.setText(resistor.getServiceUUID().toString());
	}

	private void setDeviceAddress(View row, BluetoothDeviceModel resistor)
	{
		TextView deviceAddress = row.findViewById(R.id.TextViewDeviceAddress);
		deviceAddress.setText(resistor.getAddress());
	}

	private void setConnectButton(View row, BluetoothDeviceModel bluetooth)
	{
		Button connectButton = row.findViewById(R.id.ButtonConnect);
		connectButton.setOnClickListener(v -> {
			if (bluetoothSelectItemListener != null)
				bluetoothSelectItemListener.onConnectClick(bluetooth);
		});
	}
}
