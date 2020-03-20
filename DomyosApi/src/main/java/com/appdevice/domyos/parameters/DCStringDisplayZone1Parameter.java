package com.appdevice.domyos.parameters;

import com.appdevice.domyos.parameters.DCDisplayZone1Parameter;

public class DCStringDisplayZone1Parameter extends DCDisplayZone1Parameter
{
//	public static final int DCDisplayZone1StringKM = 1;///< KN
//	public static final int DCDisplayZone1StringMI = 2;///< MI
//	public static final int DCDisplayZone1StringKCAL = 4;///< KCAL
//	public static final int DCDisplayZone1StringBPM = 5;///< BPM
//	public static final int DCDisplayZone1StringGO = 6;///< GO
//	public static final int DCDisplayZone1StringHELLO = 7;///< HELLO
//	public static final int DCDisplayZone1StringBLUETOOTH = 8;///< BLUETOOTH
//	public static final int DCDisplayZone1StringSTOP = 9;///< STOP
//	public static final int DCDisplayZone1StringCOOL = 10;///< COOL
//	public static final int DCDisplayZone1StringKMH = 11;///< KMH
//	public static final int DCDisplayZone1StringMIH = 12;///< MIH
//	public static final int DCDisplayZone1StringPAUSE = 13;///< PAUSE
//	public static final int DCDisplayZone1StringGOOD = 14;///< GOOD


	public DCStringDisplayZone1Parameter(int displayString)
	{
		super();
		
		this.zoneType = DCDisplayZoneTypeString;
		this.zoneVariable1 = displayString;
	}
}
