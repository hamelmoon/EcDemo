package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCEquipment;

public class DCSettingModeSetInfoValueCommand extends DCSetInfoValueCommand
{
	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting;
	}
}
