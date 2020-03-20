package com.appdevice.domyos.parameters.bike;

import com.appdevice.domyos.parameters.DCDisplayZone1Parameter;

public class DCClockDisplayZone1Parameter extends DCDisplayZone1Parameter
{

	public DCClockDisplayZone1Parameter(int hour, int minute)
	{
		super();

		this.zoneType = DCDisplayZoneTypeClcok;
		this.zoneVariable1 = hour * 0x100 + minute;
	}
}
