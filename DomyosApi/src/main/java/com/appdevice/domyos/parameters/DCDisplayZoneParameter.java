package com.appdevice.domyos.parameters;

public class DCDisplayZoneParameter
{

	public static final int DCDisplayZoneTypeArithmetic = 1;
	public static final int DCDisplayZoneTypeString = 2;
	public static final int DCDisplayZoneTypeClcok = 3;

	public int zoneType;
	public int zoneVariable1;
	public int zoneVariable2;

	public DCDisplayZoneParameter()
	{
		zoneType = 0xFF;
		zoneVariable1 = 0xFFFF;
		zoneVariable2 = 0xFF;
	}
}
