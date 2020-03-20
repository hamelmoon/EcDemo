package com.appdevice.domyos.parameters.bike;

import com.appdevice.domyos.parameters.treadmill.DCWorkoutModeSetInfoParameters;

public class DCBikeWorkoutModeSetInfoParameters extends DCWorkoutModeSetInfoParameters
{
	public void setTorqueResistanceLevel(int torqueResistanceLevel)
	{
		this.mTorqueResistanceLevel = (byte) torqueResistanceLevel;
	}

	public void setWatt(int watt)
	{
		this.mWatt = watt;
	}
}
