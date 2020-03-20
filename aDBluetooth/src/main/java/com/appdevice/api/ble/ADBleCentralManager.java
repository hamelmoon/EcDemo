package com.appdevice.api.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.appdevice.api.utility.ADLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ADBleCentralManager
{
	private static String TAG = "ADBle";

	private static ADBleCentralManager mInstance = null;

	private static Context mApplicationContext = null;

	private BluetoothAdapter mBluetoothAdapter = null;

	private BluetoothManager mBluetoothManager = null;

	private boolean isScanning = false;

	private ADBleCentralManagerCallback mCallback = null;

	private final ConcurrentHashMap<String, ADBlePeripheral> mBlePeripherals;

    private final List<String> mScanedAddresses;

	private ADBleService mBleService = null;

	private Intent mServiceIntent = null;

	private boolean mInitializationState;

	protected ADBleCentralManager()
	{
		mBlePeripherals = new ConcurrentHashMap<String, ADBlePeripheral>();
        mScanedAddresses = new ArrayList<String>();
	}

	public static ADBleCentralManager getInstance()
	{
		if (mInstance == null)
		{
			synchronized (ADBleCentralManager.class)
			{
				if (mInstance == null)
				{
					mInstance = new ADBleCentralManager();
				}
			}
		}
		return mInstance;
	}

	public void initialize(Context applicationContext)
	{
		if (applicationContext == null)
		{
			throw new IllegalArgumentException("applicationContext = null");
		}

		mApplicationContext = applicationContext.getApplicationContext();

		mBluetoothManager = (BluetoothManager) mApplicationContext.getSystemService(Context.BLUETOOTH_SERVICE);

		mBluetoothAdapter = mBluetoothManager.getAdapter();

		if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled())
		{
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mApplicationContext.startActivity(intent);
		}

		if (mServiceIntent == null)
		{
			mServiceIntent = new Intent(mApplicationContext, ADBleService.class);
			mApplicationContext.bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	public boolean getInitializationState()
	{
		return mInitializationState;
	}

	private ServiceConnection mServiceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			mBleService = ((ADBleService.LocalBinder) service).getService();

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
							mInitializationState = true;
							ADLog.v(TAG, "initialized");
							mCallback.bleCentralManagerDidInitialized();
						}
					});
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			mBleService = null;
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
							mInitializationState = false;
							ADLog.v(TAG, "no more initialized");
							mCallback.bleCentralManagerDidUnBound();
						}
					});
				}
			}
		}
	};

	public void setCallBack(ADBleCentralManagerCallback callBack)
	{
		synchronized (this)
		{
			mCallback = callBack;
		}
	}

	public boolean isScanning()
	{
		return isScanning;
	}

	public boolean reboundService(){
		if (mApplicationContext != null)
		{
			ADLog.v(TAG, "not initialised, restarting service ...");
			mServiceIntent = new Intent(mApplicationContext, ADBleService.class);
			return mApplicationContext.bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

		}else{
			ADLog.v(TAG, "can't initialize");
			return false;
		}
	}

	public boolean scan()
	{
		if (mApplicationContext == null || mBleService == null)
		{
			if(mApplicationContext!=null) {
				ADLog.v(TAG, "not initialised, restarting service ...");
				mServiceIntent = new Intent(mApplicationContext, ADBleService.class);
				mApplicationContext.bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
				return false;
			}else {
				throw new IllegalArgumentException("not initialized");
			}
		}

		if (mBluetoothAdapter != null)
		{
			if (!isScanning)
			{
				mScanedAddresses.clear();
				isScanning = mBluetoothAdapter.startLeScan(mLeScanCallback);
				return isScanning;
			}
		}
		return false;
	}

	public boolean cancelScan()
	{
		if (mApplicationContext == null || mBleService == null)
		{
			throw new IllegalArgumentException("not initialized");
		}

		if (mBluetoothAdapter != null)
		{
			if (isScanning)
			{

				// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				// {
				// mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
				// }
				// else
				// {
				// mBluetoothAdapter.stopLeScan(mLeScanCallback);
				// }
				mBluetoothAdapter.stopLeScan(mLeScanCallback);

				isScanning = false;
				return true;
			}
		}
		return false;
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
        {
            if (!mScanedAddresses.contains(device.getAddress()))
			{
                mScanedAddresses.add(device.getAddress());
                
                final ADBlePeripheral peripheral = getBlePeripheral(device);

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
								ADLog.v(TAG, "%s didDiscover", peripheral.getName());
								mCallback.bleCentralManagerDidDiscover(peripheral, rssi, scanRecord);
							}
						});
					}
				}
			}
		}
	};

	private ADBlePeripheral getBlePeripheral(final BluetoothDevice device)
	{
		ADBlePeripheral peripheral = null;
		if (mBlePeripherals.containsKey(device.getAddress()))
		{
			peripheral = mBlePeripherals.get(device.getAddress());
		}
		else
		{
			peripheral = new ADBlePeripheral(device, mBleService);
			mBlePeripherals.put(device.getAddress(), peripheral);
		}
		return peripheral;
	}

	private ADBlePeripheralConnectionStautsCallback mConnectionStautsCallback = new ADBlePeripheralConnectionStautsCallback()
	{
		@Override
		public void didConnectionStatusChanged(final ADBlePeripheral peripheral, final int status)
		{
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
							if (status == ADBleConnectionStatus.Connected)
							{
								ADLog.v(TAG, "%s connected", peripheral.getName());
								mCallback.bleCentralManagerDidConnectPeripheral(peripheral);
							}
							else if (status == ADBleConnectionStatus.Disconnected)
							{
								ADLog.v(TAG, "%s disconnected", peripheral.getName());
								mCallback.bleCentralManagerDidDisconnectPeripheral(peripheral);
							}
						}
					});

				}
			}
		}
	};

	public void connectPeripheral(String address)
	{
		if (mBluetoothAdapter != null)
		{
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			ADBlePeripheral peripheral = getBlePeripheral(device);
			connectPeripheral(peripheral);
		}
	}

	public void connectPeripheral(ADBlePeripheral peripheral)
	{
		if (mApplicationContext == null || mBleService == null)
		{
			throw new IllegalArgumentException("not initialized");
		}

		if (peripheral == null)
		{
			throw new IllegalArgumentException("peripheral = null");
		}
		peripheral.setConnectionStautsCallback(mConnectionStautsCallback);
		peripheral.connect();
	}

	public void cancelPeripheralConnection(ADBlePeripheral peripheral)
	{
		if (mApplicationContext == null || mBleService == null)
		{
			throw new IllegalArgumentException("not initialized");
		}

		if (peripheral == null)
		{
			throw new IllegalArgumentException("peripheral = null");
		}

		peripheral.cancelConnect();
	}

}
