package com.appdevice.domyos.commands.treadmill;

import com.appdevice.domyos.DCEquipment;

public class DCSettingModeGetTreadmillDataCommand extends DCGetTreadmillDataCommand
{
	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting;
	}
}
