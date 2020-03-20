package com.appdevice.domyos.parameters.treadmill;

import com.appdevice.domyos.parameters.DCSetInfoParameters;

public class DCWorkoutModeSetInfoParameters extends DCSetInfoParameters
{
	public void setBtLedSwitch(boolean btLedSwitchOn)
	{
		this.mBtLedSwitch = (byte) (btLedSwitchOn ? 0x01 : 0x00);
	}

	public void setHeartRateLedColor(int heartRateLedColor)
	{
		this.mHeartRateLedColor = (byte) heartRateLedColor;
	}
}
