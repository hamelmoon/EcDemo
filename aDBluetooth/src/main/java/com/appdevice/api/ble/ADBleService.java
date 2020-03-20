package com.appdevice.api.ble;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.appdevice.api.ble.ADBlePeripheral.ADWriteCharacteristicCallback;
import com.appdevice.api.utility.ADLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ADBleService extends Service
{

	private static String TAG = "ADBle";

	private final IBinder mBinder = new LocalBinder();

	private final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	private final ConcurrentHashMap<BluetoothDevice, BluetoothGatt> mBluetoothDeviceGattPairs = new ConcurrentHashMap<BluetoothDevice, BluetoothGatt>();

	private final ConcurrentLinkedQueue<ADBleOperator> mBleOperators = new ConcurrentLinkedQueue<ADBleOperator>();

	private ADBleOperator mCurrentOperator = null;

	private Handler mHandler;

	private HandlerThread mThread;

	public class LocalBinder extends Binder
	{
		ADBleService getService()
		{
			return ADBleService.this;
		}
	}

	@Override
	public void onCreate()
	{
		ADLog.v(TAG, "onCreate");
		startForeground(0, null);

		mThread = new HandlerThread("adbleService.thread");
		mThread.start();

		mHandler = new Handler(mThread.getLooper());
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		ADLog.v(TAG, "onBind");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		ADLog.v(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy()
	{
		ADLog.v(TAG, "onDestroy()");

		if (mThread != null)
		{

			mThread.quit();

		}
		super.onDestroy();
	}

	synchronized void onOperatorFinish()
	{
		mCurrentOperator = null;
		runNext();
	}

	private synchronized void runNext()
	{
		if (mCurrentOperator != null)
		{
			return;
		}

		if (!mBleOperators.isEmpty())
		{
			mCurrentOperator = mBleOperators.poll();

			mHandler.post(mCurrentOperator);
		}
	}

	private BluetoothGatt getBluetoothGatt(final BluetoothDevice bluetoothDevice)
	{
		BluetoothGatt bluetoothGatt = null;
		if (mBluetoothDeviceGattPairs.containsKey(bluetoothDevice))
		{
			bluetoothGatt = mBluetoothDeviceGattPairs.get(bluetoothDevice);
		}
		return bluetoothGatt;
	}

	synchronized void connect(BluetoothDevice bluetoothDevice, BluetoothGattCallback bluetoothGattCallback)
	{
		clearOperator(bluetoothDevice);
		mBleOperators.offer(new ADBleConnectOperator(bluetoothDevice, bluetoothGattCallback));
		runNext();
	}

	synchronized void cancelConnect(BluetoothDevice bluetoothDevice)
	{
		clearOperator(bluetoothDevice);
		BluetoothGatt bluetoothGatt = getBluetoothGatt(bluetoothDevice);
		if (bluetoothGatt == null)
		{
			return;
		}
		bluetoothGatt.disconnect();

	}

	private void clearOperator(BluetoothDevice bluetoothDevice)
	{
		List<ADBleOperator> list = new ArrayList<>();
		for (ADBleOperator operator : mBleOperators)
		{
			if (operator.getBluetoothDevice() == bluetoothDevice)
			{
				list.add(operator);
			}
		}

		mBleOperators.removeAll(list);

		if (mCurrentOperator != null && mCurrentOperator.getBluetoothDevice() == bluetoothDevice)
		{
			mCurrentOperator = null;
			runNext();
		}
	}
	
	void onDisconnect(BluetoothDevice bluetoothDevice)
	{
		clearOperator(bluetoothDevice);
		BluetoothGatt bluetoothGatt = getBluetoothGatt(bluetoothDevice);
		if (bluetoothGatt == null)
		{
			return;
		}
		bluetoothGatt.close();
		mBluetoothDeviceGattPairs.remove(bluetoothDevice);
	}
	

	void discoverServices(BluetoothDevice bluetoothDevice)
	{
		mBleOperators.offer(new ADBleDiscoverServicesOperator(bluetoothDevice));
		runNext();
	}

	void readCharacteristic(BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic)
	{
		mBleOperators.offer(new ADBleReadCharacteristicOperator(bluetoothDevice, characteristic));
		runNext();
	}

	void writeCharacteristic(BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic, byte[] bytes, ADWriteCharacteristicCallback callback)
	{
		mBleOperators.offer(new ADBleWriteCharacteristicOperator(bluetoothDevice, characteristic, bytes, callback));
		runNext();
	}

	void setCharacteristicNotification(BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic, boolean enabled)
	{
		mBleOperators.offer(new ADBleSetCharacteristicNotificationOperator(bluetoothDevice, characteristic, enabled));
		runNext();
	}

	void readRssi(BluetoothDevice bluetoothDevice)
	{
		mBleOperators.offer(new ADBleReadRemoteRssiOperator(bluetoothDevice));
		runNext();
	}

	List<BluetoothGattService> getSupportedGattServices(BluetoothDevice bluetoothDevice)
	{
		BluetoothGatt bluetoothGatt = getBluetoothGatt(bluetoothDevice);
		if (bluetoothGatt == null)
			return null;

		return bluetoothGatt.getServices();
	}

	abstract class ADBleOperator implements Runnable
	{
		private final BluetoothDevice mBluetoothDevice;

		public ADBleOperator(BluetoothDevice bluetoothDevice)
		{
			mBluetoothDevice = bluetoothDevice;
		}

		public BluetoothDevice getBluetoothDevice()
		{
			return mBluetoothDevice;
		}

		@Override
		public void run()
		{

		}

	}

	class ADBleConnectOperator extends ADBleOperator
	{
		private final BluetoothGattCallback mBluetoothGattCallback;

		ADBleConnectOperator(BluetoothDevice bluetoothDevice, BluetoothGattCallback bluetoothGattCallback)
		{
			super(bluetoothDevice);
			mBluetoothGattCallback = bluetoothGattCallback;
		}

		@Override
		public void run()
		{

			BluetoothGatt bluetoothGatt = getBluetoothGatt(super.mBluetoothDevice);
			if (bluetoothGatt != null)
			{
				ADLog.v(TAG, "%s try to reconnect", super.mBluetoothDevice.getName());
				bluetoothGatt.connect();
			}
			else
			{
				ADLog.v(TAG, "%s try to connectGatt", super.mBluetoothDevice.getName());
				bluetoothGatt = super.mBluetoothDevice.connectGatt(ADBleService.this, false, mBluetoothGattCallback);
				if (bluetoothGatt != null)
				{
					mBluetoothDeviceGattPairs.put(super.mBluetoothDevice, bluetoothGatt);
				}
			}

		}
	}

	class ADBleDiscoverServicesOperator extends ADBleOperator
	{

		ADBleDiscoverServicesOperator(BluetoothDevice bluetoothDevice)
		{
			super(bluetoothDevice);
		}

		@Override
		public void run()
		{
			BluetoothGatt bluetoothGatt = getBluetoothGatt(super.mBluetoothDevice);
			if (bluetoothGatt == null)
			{
				return;
			}
			ADLog.v(TAG, "%s discoverServices", super.mBluetoothDevice.getName());
			bluetoothGatt.discoverServices();
		}
	}

	class ADBleReadCharacteristicOperator extends ADBleOperator
	{

		private final BluetoothGattCharacteristic mCharacteristic;

		ADBleReadCharacteristicOperator(BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic)
		{
			super(bluetoothDevice);
			mCharacteristic = characteristic;
		}

		@Override
		public void run()
		{
			BluetoothGatt bluetoothGatt = getBluetoothGatt(super.mBluetoothDevice);
			if (bluetoothGatt == null)
			{
				return;
			}
			bluetoothGatt.readCharacteristic(mCharacteristic);
		}
	}

	

	class ADBleWriteCharacteristicOperator extends ADBleOperator
	{

		private final BluetoothGattCharacteristic mCharacteristic;

		private final byte[] mBytes;

		private final ADWriteCharacteristicCallback mCallback;

		ADBleWriteCharacteristicOperator(BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic, byte[] bytes, ADWriteCharacteristicCallback callback)
		{
			super(bluetoothDevice);
			mCharacteristic = characteristic;
			mBytes = bytes;
			mCallback = callback;
		}

		@Override
		public void run()
		{
			ADLog.v(TAG, "%s writeValueForCharacteristic", super.mBluetoothDevice.getName());
			BluetoothGatt bluetoothGatt = getBluetoothGatt(super.mBluetoothDevice);
			if (bluetoothGatt == null && mBytes != null)
			{
				return;
			}

			int properties = mCharacteristic.getProperties();

			if ((properties & BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) > 0)
			{
				mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
			}
			else
			{
				mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
			}

			mCharacteristic.setValue(mBytes);

			bluetoothGatt.writeCharacteristic(mCharacteristic);

			if (mCallback != null)
			{
				mCallback.writeCharacteristicFinish();
			}
		}
	}

	class ADBleSetCharacteristicNotificationOperator extends ADBleOperator
	{

		private final BluetoothGattCharacteristic mCharacteristic;

		private final boolean mEnabled;

		ADBleSetCharacteristicNotificationOperator(BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic, boolean enabled)
		{
			super(bluetoothDevice);
			mCharacteristic = characteristic;
			mEnabled = enabled;
		}

		@Override
		public void run()
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			BluetoothGatt bluetoothGatt = getBluetoothGatt(super.mBluetoothDevice);
			if (bluetoothGatt == null)
			{
				return;
			}
			bluetoothGatt.setCharacteristicNotification(mCharacteristic, mEnabled);

			BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

			if (descriptor != null)
			{
				byte[] descriptorValue = descriptor.getValue();
				if (mEnabled)
				{
					if (!Arrays.equals(descriptorValue, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE))
					{
						descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					}
				}
				else
				{
					if (!Arrays.equals(descriptorValue, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE))
					{
						descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
					}
				}
				ADLog.v(TAG, "%s writeDescriptor", super.mBluetoothDevice.getName());
				bluetoothGatt.writeDescriptor(descriptor);
			}
		}
	}

	class ADBleReadRemoteRssiOperator extends ADBleOperator
	{

		ADBleReadRemoteRssiOperator(BluetoothDevice bluetoothDevice)
		{
			super(bluetoothDevice);
		}

		@Override
		public void run()
		{
			ADLog.v(TAG, "%s readRemoteRssi", super.mBluetoothDevice.getName());
			BluetoothGatt bluetoothGatt = getBluetoothGatt(super.mBluetoothDevice);
			if (bluetoothGatt == null)
			{
				return;
			}

			bluetoothGatt.readRemoteRssi();
		}
	}
}
