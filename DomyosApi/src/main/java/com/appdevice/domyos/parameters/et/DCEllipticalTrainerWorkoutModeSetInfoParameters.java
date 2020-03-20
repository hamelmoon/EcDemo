package com.appdevice.domyos.parameters.et;

import com.appdevice.domyos.commands.DCSettingModeSetInfoParameters;
import com.appdevice.domyos.parameters.treadmill.DCWorkoutModeSetInfoParameters;

public class DCEllipticalTrainerWorkoutModeSetInfoParameters extends DCSettingModeSetInfoParameters
{
	public void setTorqueResistanceLevel(int torqueResistanceLevel)
	{
		this.mTorqueResistanceLevel = (byte) torqueResistanceLevel;
	}

    public void setWatt(int watt) 
	{
        this.mWatt = watt;
    }

    public void setIncline(int incline) 
	{
        this.mIncline = incline;
    }
}
