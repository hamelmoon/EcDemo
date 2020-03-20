package com.appdevice.domyos.commands.treadmill;

import com.appdevice.domyos.DCEquipment;

public class DCWorkoutModeGetTreadmillDataCommand extends DCGetTreadmillDataCommand
{
	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeWorkout;
	}
}
