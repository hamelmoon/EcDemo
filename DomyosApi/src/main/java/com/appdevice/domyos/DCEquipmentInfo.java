package com.appdevice.domyos;

public class DCEquipmentInfo
{
	private float mFirmwareVersion;

	private String mSerialNumber;

	private int mUsageHour;

	private int mCumulativeKM;

	public float getFirmwareVersion()
	{
		return mFirmwareVersion;
	}

	void setFirmwareVersion(float firmwareVersion)
	{
		this.mFirmwareVersion = firmwareVersion;
	}

	public String getSerialNumber()
	{
		return mSerialNumber;
	}

	void setSerialNumber(String serialNumber)
	{
		this.mSerialNumber = serialNumber;
	}

	public int getUsageHour()
	{
		return mUsageHour;
	}

	void setUsageHour(int usageHour)
	{
		this.mUsageHour = usageHour;
	}

	public int getCumulativeKM()
	{
		return mCumulativeKM;
	}

	void setCumulativeKM(int cumulativeKM)
	{
		this.mCumulativeKM = cumulativeKM;
	}
}
