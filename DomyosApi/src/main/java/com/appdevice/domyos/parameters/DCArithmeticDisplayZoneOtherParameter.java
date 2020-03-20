package com.appdevice.domyos.parameters;

public class DCArithmeticDisplayZoneOtherParameter extends DCDisplayZoneOtherParameter
{
	public static final int DCDisplayZone1ArithmeticTypeInteger = 0;
	public static final int DCDisplayZoneOtherArithmeticTypeOneDecimalPlace = 1;
	public static final int DCDisplayZone1ArithmeticTypeTwoDecimalPlace = 2;

	public DCArithmeticDisplayZoneOtherParameter(int arithmeticType, float value)
	{
		super();

		this.zoneType = DCDisplayZoneTypeArithmetic;

		this.zoneVariable2 = arithmeticType;

		if (this.zoneVariable2 == DCDisplayZone1ArithmeticTypeInteger)
		{
			this.zoneVariable1 = (int) value;
		}
		else if (this.zoneVariable2 == DCDisplayZoneOtherArithmeticTypeOneDecimalPlace)
		{
			this.zoneVariable1 = (int) (value * 10);
		}
		else if (this.zoneVariable2 == DCDisplayZone1ArithmeticTypeTwoDecimalPlace)
		{
			this.zoneVariable1 = (int) (value * 100);
		}
	}
}
