package com.appdevice.domyos.commands;

import com.appdevice.domyos.parameters.DCSetInfoParameters;

public class DCSettingModeSetInfoParameters extends DCSetInfoParameters
{
	public void setBtLedSwitch(boolean btLedSwitchOn)
	{
		this.mBtLedSwitch = (byte) (btLedSwitchOn ? 0x01 : 0x00);
	}
	
	public void setHeartRateLedColor(int heartRateLedColor)
	{
		this.mHeartRateLedColor = (byte)heartRateLedColor;
	}
}
