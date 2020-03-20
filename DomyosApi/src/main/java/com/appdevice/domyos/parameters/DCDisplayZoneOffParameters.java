package com.appdevice.domyos.parameters;

public class DCDisplayZoneOffParameters
{
	public byte mDisplayZone1OffParameter;
	public byte mDisplayZone2OffParameter;
	public byte mDisplayZone3OffParameter;
	public byte mDisplayZone4OffParameter;
	public byte mDisplayZone5OffParameter;
	public byte mDisplayZone6OffParameter;

	public DCDisplayZoneOffParameters()
	{
		mDisplayZone1OffParameter = (byte) 0xFF;
		mDisplayZone2OffParameter = (byte) 0xFF;
		mDisplayZone3OffParameter = (byte) 0xFF;
		mDisplayZone4OffParameter = (byte) 0xFF;
		mDisplayZone5OffParameter = (byte) 0xFF;
		mDisplayZone6OffParameter = (byte) 0xFF;
	}

	public void setDisplayZone1Off()
	{
		this.mDisplayZone1OffParameter = (byte) 1;
	}

	public void setDisplayZone2Off()
	{
		this.mDisplayZone2OffParameter = (byte) 1;
	}

	public void setDisplayZone3Off()
	{
		this.mDisplayZone3OffParameter = (byte) 1;
	}

	public void setDisplayZone4Off()
	{
		this.mDisplayZone4OffParameter = (byte) 1;
	}

	public void setDisplayZone5Off()
	{
		this.mDisplayZone5OffParameter = (byte) 1;
	}

	public void setDisplayZone6Off()
	{
		this.mDisplayZone6OffParameter = (byte) 1;
	}
}
