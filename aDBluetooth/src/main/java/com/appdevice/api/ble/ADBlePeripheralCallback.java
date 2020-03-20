package com.appdevice.api.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public interface ADBlePeripheralCallback
{
	void peripheralDidReadRSSI(final ADBlePeripheral peripheral, int rssi, int status);

	void peripheralDidDiscoverServices(final ADBlePeripheral peripheral, BluetoothGattService[] services, int status);

	void peripheralDidWriteValueForDescriptor(final ADBlePeripheral peripheral, final BluetoothGattDescriptor descriptor, int status);

	void peripheralDidWriteValueForCharacteristic(final ADBlePeripheral peripheral, final BluetoothGattCharacteristic characteristic, int status);

	void peripheralDidUpdateValueForCharacteristic(final ADBlePeripheral peripheral, final BluetoothGattCharacteristic characteristic,final byte[] value);

	void peripheralDidReadValueForCharacteristic(final ADBlePeripheral peripheral, final BluetoothGattCharacteristic characteristic, int status);
}
