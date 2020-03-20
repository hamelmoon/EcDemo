package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCEquipment;

public class DCRebootConsoleExCommand extends DCRebootConsoleCommand
{
	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
	}
}
