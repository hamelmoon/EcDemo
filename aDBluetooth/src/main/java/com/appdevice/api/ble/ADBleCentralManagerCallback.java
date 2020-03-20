package com.appdevice.api.ble;

import android.annotation.TargetApi;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public interface ADBleCentralManagerCallback
{
	
	public void bleCentralManagerDidInitialized();
	public void bleCentralManagerDidUnBound();

	public void bleCentralManagerDidDiscover(final ADBlePeripheral peripheral, int rssi, byte[] scanRecord);

	public void bleCentralManagerDidConnectPeripheral(final ADBlePeripheral peripheral);

	public void bleCentralManagerDidDisconnectPeripheral(final ADBlePeripheral peripheral);

}
