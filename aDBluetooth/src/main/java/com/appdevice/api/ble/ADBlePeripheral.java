package com.appdevice.api.ble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.appdevice.api.utility.ADConverter;
import com.appdevice.api.utility.ADLog;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ADBlePeripheral
{
	private static String TAG = "ADBle";

	private int mBleConnectionStatus;

	private ADBlePeripheralConnectionStautsCallback mConnectionStautsCallback;

	private BluetoothDevice mBluetoothDevice = null;

	private List<BluetoothGattService> mServices = new ArrayList<BluetoothGattService>();

	private ADBlePeripheralCallback mCallback = null;

	private ADBleService mBleService = null;

	ADBlePeripheral(BluetoothDevice bluetoothDevice, ADBleService bleService)
	{
		if (bluetoothDevice == null)
		{
			throw new IllegalArgumentException("bluetoothDevice = null");
		}

		if (bleService == null)
		{
			throw new IllegalArgumentException("bleService = null");
		}

		mBluetoothDevice = bluetoothDevice;
		mBleService = bleService;

	}

	public void setCallback(ADBlePeripheralCallback callback)
	{
		synchronized (this)
		{
			mCallback = callback;
		}
	}

	void setConnectionStautsCallback(ADBlePeripheralConnectionStautsCallback connectionStautsCallback)
	{
		synchronized (this)
		{
			mConnectionStautsCallback = connectionStautsCallback;
		}
	}

	void connect()
	{
		if (mBleConnectionStatus == ADBleConnectionStatus.Disconnected)
		{
			setBleConnectionStatus(ADBleConnectionStatus.Connecting);
			mBleService.connect(mBluetoothDevice, mGattCallback);
		}
	}

	void cancelConnect()
	{
		if (mBleConnectionStatus == ADBleConnectionStatus.Disconnected)
		{
			return;
		}
		mBleService.cancelConnect(mBluetoothDevice);
	}

	void setBleConnectionStatus(int status)
	{
		if (mBleConnectionStatus != status)
		{
			mBleConnectionStatus = status;
			synchronized (this)
			{
				if (mConnectionStautsCallback != null)
				{
					mConnectionStautsCallback.didConnectionStatusChanged(this, status);
				}
			}
		}
	}

	public int getBleConnectionStatus()
	{
		return mBleConnectionStatus;
	}

	public String getName()
	{
		return mBluetoothDevice.getName();
	}

	public String getAddress()
	{
		return mBluetoothDevice.getAddress();
	}

	public BluetoothGattService[] getService()
	{
		return mServices.toArray(new BluetoothGattService[mServices.size()]);
	}

	public void discoverServices()
	{
		if (mBleConnectionStatus != ADBleConnectionStatus.Connected)
		{
			return;
		}
		synchronized (this)
		{
			mServices.clear();
		}
		if (mBleService != null && mBluetoothDevice != null)
		{
			mBleService.discoverServices(mBluetoothDevice);
		}
	}

	public void readRssi()
	{
		if (mBleConnectionStatus != ADBleConnectionStatus.Connected)
		{
			return;
		}
		if (mBleService != null && mBluetoothDevice != null)
		{
			mBleService.readRssi(mBluetoothDevice);
		}
	}

	public interface ADWriteCharacteristicCallback
	{
		void writeCharacteristicFinish();
	}

	public void writeValueForCharacteristic(BluetoothGattCharacteristic characteristic, byte[] bytes, ADWriteCharacteristicCallback callback)
	{
		if (mBleConnectionStatus != ADBleConnectionStatus.Connected)
		{
			return;
		}
		if (mBleService != null && mBluetoothDevice != null)
		{
			final int perTransactionLength = 20;
			int startIndex = 0;
			int stopIndex = 0;
			byte[] writeData;
			while (true)
			{
				if (startIndex + perTransactionLength <= bytes.length)
				{
					stopIndex = startIndex + perTransactionLength;
				}
				else
				{
					stopIndex = startIndex + bytes.length - startIndex;
				}
				writeData = Arrays.copyOfRange(bytes, startIndex, stopIndex);

				if (mBleConnectionStatus != ADBleConnectionStatus.Connected)
				{
					return;
				}

				if (stopIndex >= bytes.length)
				{
					mBleService.writeCharacteristic(mBluetoothDevice, characteristic, writeData, callback);
					break;
				}
				else
				{
					mBleService.writeCharacteristic(mBluetoothDevice, characteristic, writeData, null);
				}
				startIndex += perTransactionLength;
			}

		}
	}

	public void readValueForCharacteristic(BluetoothGattCharacteristic characteristic)
	{
		if (mBleConnectionStatus != ADBleConnectionStatus.Connected)
		{
			return;
		}
		if (mBleService != null && mBluetoothDevice != null)
		{
			mBleService.readCharacteristic(mBluetoothDevice, characteristic);
		}
	}

	public void setNotifyValueForCharacteristic(BluetoothGattCharacteristic characteristic, boolean enabled)
	{
		if (mBleConnectionStatus != ADBleConnectionStatus.Connected)
		{
			return;
		}
		if (mBleService != null && mBluetoothDevice != null)
		{
			mBleService.setCharacteristicNotification(mBluetoothDevice, characteristic, enabled);
		}
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
	{
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
		{
			ADLog.v(TAG, "%s onConnectionStateChange() %d -> %d", getName(), status, newState);
			if (newState != 3)
			{
				if (newState == 0)
				{
					mBleService.onDisconnect(mBluetoothDevice);
				}

				if (mBleConnectionStatus == ADBleConnectionStatus.Connecting && (status == 0 || status == 133))
				{
					mBleService.onOperatorFinish();
				}

				setBleConnectionStatus(newState);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, final int status)
		{
			ADLog.v(TAG, "%s onServicesDiscovered status %d", getName(), status);
			synchronized (this)
			{
				mServices.addAll(gatt.getServices());
				if (mCallback != null)
				{
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable()
					{

						@Override
						public void run()
						{
							mCallback.peripheralDidDiscoverServices(ADBlePeripheral.this, getService(), status);
						}
					});
				}
			}
			mBleService.onOperatorFinish();
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status)
		{
			ADLog.v(TAG, "%s onCharacteristicRead status %d", getName(), status);
			synchronized (this)
			{
				if (mCallback != null)
				{
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable()
					{

						@Override
						public void run()
						{
							mCallback.peripheralDidReadValueForCharacteristic(ADBlePeripheral.this, characteristic, status);
						}
					});
				}
			}
			mBleService.onOperatorFinish();
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status)
		{
			ADLog.v(TAG, "%s onCharacteristicWrite status %d", getName(), status);
			synchronized (this)
			{
				if (mCallback != null)
				{
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable()
					{

						@Override
						public void run()
						{
							mCallback.peripheralDidWriteValueForCharacteristic(ADBlePeripheral.this, characteristic, status);
						}
					});
				}
			}
			mBleService.onOperatorFinish();
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic)
		{
			//ADLog.v(TAG, "%s onCharacteristicChanged %s", getName(), ADConverter.byteArrayToHexString(characteristic.getValue()));
			final byte[] value = characteristic.getValue();
			synchronized (this)
			{
				if (mCallback != null)
				{
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable()
					{

						@Override
						public void run()
						{
							mCallback.peripheralDidUpdateValueForCharacteristic(ADBlePeripheral.this, characteristic, value);
						}
					});
				}
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status)
		{
			ADLog.v(TAG, "%s onDescriptorWrite status %d", getName(), status);
			synchronized (this)
			{
				if (mCallback != null)
				{
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable()
					{

						@Override
						public void run()
						{
							mCallback.peripheralDidWriteValueForDescriptor(ADBlePeripheral.this, descriptor, status);
						}
					});
				}
			}
			mBleService.onOperatorFinish();
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, final int status)
		{
			ADLog.v(TAG, "%s onReadRemoteRssi rssi %d status %d", getName(), rssi, status);
			synchronized (this)
			{
				if (mCallback != null)
				{
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable()
					{

						@Override
						public void run()
						{
							mCallback.peripheralDidReadRSSI(ADBlePeripheral.this, rssi, status);
						}
					});
				}
			}
			mBleService.onOperatorFinish();
		}

	};
}
