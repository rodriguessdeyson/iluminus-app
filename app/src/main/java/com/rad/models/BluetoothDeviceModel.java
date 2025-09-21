package com.rad.models;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.UUID;

public class BluetoothDeviceModel implements Parcelable {
	/**
	 * Id of the register.
	 */
	private int id;
	
	/**
	 * name of Bluetooth device.
	 */
	private String name;
	
	/**
	 * Address of the device.
	 */
	private String address;
	
	/**
	 * Supported service uuid of the device.
	 */
	private UUID serviceUUID;
	
	/**
	 * Bluetooth device reference.
	 */
	private BluetoothDevice device;
	
	public BluetoothDeviceModel(String name, String address, UUID serviceUUID, BluetoothDevice device)
	{
		setName(name);
		setAddress(address);
		setServiceUUID(serviceUUID);
		setDevice(device);
	}

	public BluetoothDeviceModel(String name, String address, BluetoothDevice device)
	{
		setName(name);
		setAddress(address);
		setDevice(device);
	}

	protected BluetoothDeviceModel(Parcel in) {
		id = in.readInt();
		name = in.readString();
		address = in.readString();
		device = in.readParcelable(BluetoothDevice.class.getClassLoader());
	}

	public static final Creator<BluetoothDeviceModel> CREATOR = new Creator<BluetoothDeviceModel>() {
		@Override
		public BluetoothDeviceModel createFromParcel(Parcel in) {
			return new BluetoothDeviceModel(in);
		}
		
		@Override
		public BluetoothDeviceModel[] newArray(int size) {
			return new BluetoothDeviceModel[size];
		}
	};

	/**
	 * Set and Id.
	 * @param id Id of row.
	 */
	public void setId(int id)
	{
		this.id = id;
	}
	
	/**
	 * Set the device address.
	 * @param address Address of the device.
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}
	
	/**
	 * Set the device name.
	 * @param name name of the device.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Set the identifier of the device service.
	 * @param serviceUUID UUID of the used service.
	 */
	public void setServiceUUID(UUID serviceUUID)
	{
		this.serviceUUID = serviceUUID;
	}
	
	/**
	 * Set the reference of the bluetooth device.
	 * @param device Bluetooth device of the configured model.
	 */
	public void setDevice(BluetoothDevice device)
	{
		this.device = device;
	}
	
	/**
	 * Get the device Id.
	 * @return Id of the row.
	 */
	public int getId()
	{
		return this.id;
	}
	
	/**
	 * Get the device UUID.
	 * @return UUID of the device service.
	 */
	public UUID getServiceUUID()
	{
		return this.serviceUUID;
	}
	
	/**
	 * Get the Bluetooth device name.
	 * @return Bluetooth device name.
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Get the Bluetooth device address.
	 * @return The Address of the Bluetooth.
	 */
	public String getAddress()
	{
		return this.address;
	}
	
	/**
	 * Get the Bluetooth device configured reference.
	 * @return Bluetooth device reference.
	 */
	public BluetoothDevice getDevice()
	{
		return this.device;
	}
	
	@NonNull
	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(address);
		dest.writeParcelable(device, flags);
	}
}
