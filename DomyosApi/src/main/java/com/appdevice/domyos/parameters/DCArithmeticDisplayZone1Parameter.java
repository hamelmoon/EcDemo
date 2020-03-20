package com.appdevice.domyos.parameters;

public class DCArithmeticDisplayZone1Parameter extends DCDisplayZone1Parameter
{
	public static final int DCDisplayZone1ArithmeticTypeInteger = 0;
	public static final int DCDisplayZone1ArithmeticTypeTwoDecimalPlace = 2;

	public DCArithmeticDisplayZone1Parameter(int arithmeticType, float value)
	{
		super();
		
		this.zoneType = DCDisplayZoneTypeArithmetic;
		
		this.zoneVariable2 = arithmeticType;

		if (this.zoneVariable2 == DCDisplayZone1ArithmeticTypeInteger)
		{
			this.zoneVariable1 = (int) value;
		}
		else if (this.zoneVariable2 == DCDisplayZone1ArithmeticTypeTwoDecimalPlace)
		{
			this.zoneVariable1 = (int) (value * 100);
		}
	}
}
