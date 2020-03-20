package com.appdevice.api.ble;

import android.annotation.TargetApi;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ADBleConnectionStatus
{
	public static final int Disconnected = 0;
	public static final int Connecting = 1;
	public static final int Connected = 2;
}
