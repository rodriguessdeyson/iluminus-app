package com.rad.listeners;

import com.rad.models.BluetoothDeviceModel;

public interface BluetoothSelectItemListener
{
	/**
	 * Called when the connect button is clicked
	 * @param bluetoothDeviceModel The bluetooth device model
	 */
	void onConnectClick(BluetoothDeviceModel bluetoothDeviceModel);
}
