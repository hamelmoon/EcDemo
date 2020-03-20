package com.appdevice.domyos.parameters;

/**
 * Created by apple on 2016/12/6.
 */

public class DCEquipmentDisplayZoneOffSecondAreaParameters
{
	int _displayZone7OffParameter;
	int _displayZone8OffParameter;
	int _displayZone9OffParameter;
	int _displayZone10OffParameter;
	int _displayZone11OffParameter;
	int _displayZone12OffParameter;

	public DCEquipmentDisplayZoneOffSecondAreaParameters()
	{
		_displayZone7OffParameter = 0xFF;
		_displayZone8OffParameter = 0xFF;
		_displayZone9OffParameter = 0xFF;
		_displayZone10OffParameter = 0xFF;
		_displayZone11OffParameter = 0xFF;
		_displayZone12OffParameter = 0xFF;
	}

	public void setDisplayZone7Off()
	{
		_displayZone7OffParameter = 0x01;
	}

	public void setDisplayZone8Off()
	{
		_displayZone8OffParameter = 0x01;
	}

	public void setDisplayZone9Off()
	{
		_displayZone9OffParameter = 0x01;
	}

	public void setDisplayZone10Off()
	{
		_displayZone10OffParameter = 0x01;
	}

	public void setDisplayZone11Off()
	{
		_displayZone11OffParameter = 0x01;
	}

	public void setDisplayZone12Off()
	{
		_displayZone12OffParameter = 0x01;
	}

	public int getDisplayZone7Off()
	{
		return _displayZone7OffParameter;
	}

	public int getDisplayZone8Off()
	{
		return _displayZone8OffParameter;
	}

	public int getDisplayZone9Off()
	{
		return _displayZone9OffParameter;
	}

	public int getDisplayZone10Off()
	{
		return _displayZone10OffParameter;
	}

	public int getDisplayZone11Off()
	{
		return _displayZone11OffParameter;
	}

	public int getDisplayZone12Off()
	{
		return _displayZone12OffParameter;
	}
}
