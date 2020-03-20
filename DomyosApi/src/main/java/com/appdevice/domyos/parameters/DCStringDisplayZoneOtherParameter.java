package com.appdevice.domyos.parameters;

import com.appdevice.domyos.parameters.DCDisplayZoneOtherParameter;

public class DCStringDisplayZoneOtherParameter extends DCDisplayZoneOtherParameter
{
//	public static final int DCDisplayZoneOtherStringKM = 1;// /< KN
//	public static final int DCDisplayZoneOtherStringMI = 2;// /< MI
//	public static final int DCDisplayZoneOtherStringCAL = 4;// /< CAL
//	public static final int DCDisplayZoneOtherStringBPM = 5;// /< BPM
//	public static final int DCDisplayZoneOtherStringWAT = 6;// /< WAT
//	public static final int DCDisplayZoneOtherStringRPM = 7;// /< RPM
//	public static final int DCDisplayZoneOtherStringGO = 8;// /< GO
//	public static final int DCDisplayZoneOtherStringKMH = 11;// /< KMH
//	public static final int DCDisplayZoneOtherStringMIH = 12;// /< MIH

	public DCStringDisplayZoneOtherParameter(int displayString)
	{
		super();
		
		this.zoneType = DCDisplayZoneTypeString;
		this.zoneVariable1 = displayString;
	}
}
