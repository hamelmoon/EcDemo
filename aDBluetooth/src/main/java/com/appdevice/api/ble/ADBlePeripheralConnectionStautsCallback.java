package com.appdevice.api.ble;

import android.annotation.TargetApi;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
interface ADBlePeripheralConnectionStautsCallback
{
	void didConnectionStatusChanged(final ADBlePeripheral peripheral, int status);
}
