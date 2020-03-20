package com.appdevice.domyos.parameters;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.commands.DCSetInfoValueCommand;

public class DCWorkoutModeSetInfoValueCommand extends DCSetInfoValueCommand
{
	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeWorkout;
	}
}
