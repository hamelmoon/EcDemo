package com.appdevice.domyos.parameters.rower;

import com.appdevice.domyos.parameters.treadmill.DCWorkoutModeSetInfoParameters;

/**
 * Created by apple on 2017/5/3.
 */

public class DCRowerWorkoutModeSetInfoParameters extends DCWorkoutModeSetInfoParameters
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
