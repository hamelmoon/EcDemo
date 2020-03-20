package com.appdevice.domyos;

import android.content.Context;

import com.appdevice.api.ble.ADBleCentralManager;
import com.appdevice.api.ble.ADBleCentralManagerCallback;
import com.appdevice.api.ble.ADBlePeripheral;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class DCEquipmentManager
{

	public interface DCEquipmentManagerCallback
	{

		void equipmentManagerDidUnBound();

		void equipmentManagerDidInitialized();

		void equipmentManagerDidDiscoverEquipment(DCEquipment equipment);

		void equipmentManagerDidConnectEquipment(DCEquipment equipment);

		void equipmentManagerDidDisconnectEquipment(DCEquipment equipment);

	}

	DCEquipmentManagerCallback mCallback = null;
	private static DCEquipmentManager mInstance = null;
	private boolean mToScan;
	private final ConcurrentHashMap<String, DCEquipment> mEquipments = new ConcurrentHashMap<String, DCEquipment>();
	private final ConcurrentHashMap<String, DCEquipment> mScannedEquipments = new ConcurrentHashMap<String, DCEquipment>();

	protected DCEquipmentManager()
	{
		ADBleCentralManager.getInstance().setCallBack(new ADBleCentralManagerCallback()
		{
			@Override public void bleCentralManagerDidUnBound() {
				if (mCallback != null)
				{
					mCallback.equipmentManagerDidUnBound();
				}
			}

			@Override
			public void bleCentralManagerDidDiscover(ADBlePeripheral peripheral, int rssi, byte[] scanRecord)
			{
				if (peripheral.getName() != null)
				{
					DCEquipment equipment = null;

					if (!mEquipments.containsKey(peripheral.getAddress()))
					{
						String lowercaseName = peripheral.getName().toLowerCase();
						if (lowercaseName.startsWith("domyos-bike-"))
						{
							equipment = new DCBike();
						}
						else if (lowercaseName.startsWith("domyos-el-"))
						{
							equipment = new DCEllipticalTrainer();
						}
						else if (lowercaseName.startsWith("domyos-tc-"))
						{
							equipment = new DCTreadmill();
						}
						else if (lowercaseName.startsWith("domyos-row-"))
						{
							equipment = new DCRower();
						}

						if (equipment != null)
						{
							equipment.mPeripheral = peripheral;

							mEquipments.put(peripheral.getAddress(), equipment);
						}
					}
					else
					{
						equipment = mEquipments.get(peripheral.getAddress());
					}

					if (equipment != null)
					{
						equipment.setScanningRSSI(rssi);

						if (!mScannedEquipments.containsKey(peripheral.getAddress()))
						{
							mScannedEquipments.put(peripheral.getAddress(), equipment);

							if (mCallback != null)
							{
								mCallback.equipmentManagerDidDiscoverEquipment(equipment);
							}
						}
					}
				}
			}

			@Override
			public void bleCentralManagerDidDisconnectPeripheral(ADBlePeripheral peripheral)
			{
				DCEquipment equipment = mEquipments.get(peripheral.getAddress());
				if (equipment != null)
				{
					if (equipment.mIsManualDisconnect)
					{
						equipment.mIsManualDisconnect = false;
					}
					else
					{
						if (equipment.mAutoLinkBackEnable)
						{
							connectEquipment(equipment);
						}
					}
					if (mCallback != null)
					{
						mCallback.equipmentManagerDidDisconnectEquipment(equipment);
					}
				}
			}

			@Override
			public void bleCentralManagerDidConnectPeripheral(ADBlePeripheral peripheral)
			{
				DCEquipment equipment = mEquipments.get(peripheral.getAddress());
				if (equipment != null)
				{
					peripheral.setCallback(equipment.mBlePeripheralCallback);
					peripheral.discoverServices();
				}
			}

			@Override
			public void bleCentralManagerDidInitialized()
			{
				if (mCallback != null)
				{
					mCallback.equipmentManagerDidInitialized();
				}
			}
		});
	}

	public static DCEquipmentManager getInstance()
	{
		if (mInstance == null)
		{
			synchronized (DCEquipmentManager.class)
			{
				if (mInstance == null)
				{
					mInstance = new DCEquipmentManager();
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

		ADBleCentralManager.getInstance().initialize(applicationContext);
	}

	public boolean getInitializationState()
	{
		return ADBleCentralManager.getInstance().getInitializationState();
	}

	public void setCallBack(DCEquipmentManagerCallback callBack)
	{
		mCallback = callBack;
	}

	public int getApiMainVersion()
	{
		return 0;
	}

	public int getApiSubVersion()
	{
		return 2;
	}

	public void scanEquipments()
	{
		if (!mToScan)
		{
			mScannedEquipments.clear();
			mToScan = ADBleCentralManager.getInstance().scan();
		}
	}

	public boolean isScanning()
	{
		return mToScan;
	}

	public void stopScanEquipments()
	{
		if (mToScan)
		{
			mToScan = false;
			ADBleCentralManager.getInstance().cancelScan();
		}
	}

	public void connectEquipment(DCEquipment equipment)
	{
		if (equipment != null && equipment.mPeripheral != null)
		{
			if (equipment.getConnectionState() != DCEquipment.DCEquipmentConnectionStateConnected)
			{
				equipment.resetEquipment();
			}
			ADBleCentralManager.getInstance().connectPeripheral(equipment.mPeripheral);
		}
	}

	public boolean connectEquipment(String macAddress)
	{
		if (macAddress == null)
			return false;

		for (DCEquipment eq : getEquipments())
			if (eq.getAddress().equals(macAddress) && eq.mPeripheral != null)
			{
				if (eq.getConnectionState() != DCEquipment.DCEquipmentConnectionStateConnected)
				{
					eq.resetEquipment();
				}
				ADBleCentralManager.getInstance().connectPeripheral(eq.mPeripheral);
				return true;
			}

		return false;
	}

	public void cancelEquipmentConnection(DCEquipment equipment)
	{
		if (equipment != null && equipment.mPeripheral != null)
		{
			equipment.mIsManualDisconnect = true;
			ADBleCentralManager.getInstance().cancelPeripheralConnection(equipment.mPeripheral);
		}
	}

	public Collection<DCEquipment> getEquipments()
	{
		return mScannedEquipments.values();
	}
}
