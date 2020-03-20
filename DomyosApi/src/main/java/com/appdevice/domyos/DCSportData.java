package com.appdevice.domyos;

public class DCSportData
{

	private int mDecupleCurrentSpeedKmPerHour;

	private int mDecupleCurrentSessionCumulativeKCal;

	private int mDecupleCurrentSessionCumulativeKM;

	private int mAnalogHeartRate;

	private int mDecupleCurrentSessionAverageSpeed;

	private int mCount;

	protected DCSportDataListener mSportDataListener;

	public float getCurrentSpeedKmPerHour()
	{
		return mDecupleCurrentSpeedKmPerHour / 10.0f;
	}

	void setCurrentSpeedKmPerHour(float currentSpeedKmPerHour)
	{
		int decupleCurrentSpeedKmPerHour = (int) (currentSpeedKmPerHour * 10);
		if (this.mDecupleCurrentSpeedKmPerHour != decupleCurrentSpeedKmPerHour)
		{
			this.mDecupleCurrentSpeedKmPerHour = decupleCurrentSpeedKmPerHour;

			if (mSportDataListener != null)
			{
				mSportDataListener.onCurrentSpeedKmPerHourChanged(currentSpeedKmPerHour);
			}
		}
	}

	public int getCurrentSessionCumulativeKCal()
	{
		return mDecupleCurrentSessionCumulativeKCal;
	}

	void setCurrentSessionCumulativeKCal(int currentSessionCumulativeKCal)
	{
		int decupleCurrentSessionCumulativeKCal = currentSessionCumulativeKCal;
		if (this.mDecupleCurrentSessionCumulativeKCal != decupleCurrentSessionCumulativeKCal)
		{
			this.mDecupleCurrentSessionCumulativeKCal = decupleCurrentSessionCumulativeKCal;

			if (mSportDataListener != null)
			{
				mSportDataListener.onCurrentSessionCumulativeKCalChanged(decupleCurrentSessionCumulativeKCal);
			}
		}
	}

	public float getCurrentSessionCumulativeKM()
	{
		return mDecupleCurrentSessionCumulativeKM / 10.0f;
	}

	void setCurrentSessionCumulativeKM(float currentSessionCumulativeKM)
	{
		int decupleCurrentSessionCumulativeKM = (int) (currentSessionCumulativeKM * 10);
		if (this.mDecupleCurrentSessionCumulativeKM != decupleCurrentSessionCumulativeKM)
		{
			this.mDecupleCurrentSessionCumulativeKM = decupleCurrentSessionCumulativeKM;

			if (mSportDataListener != null)
			{
				mSportDataListener.onCurrentSessionCumulativeKMChanged(currentSessionCumulativeKM);
			}
		}
	}

	public int getAnalogHeartRate()
	{
		return mAnalogHeartRate;
	}

	void setAnalogHeartRate(int analogHeartRate)
	{
		if (this.mAnalogHeartRate != analogHeartRate)
		{
			this.mAnalogHeartRate = analogHeartRate;

			if (mSportDataListener != null)
			{
				mSportDataListener.onAnalogHeartRateChanged(analogHeartRate);
			}
		}
	}

	public float getCurrentSessionAverageSpeed()
	{
		return mDecupleCurrentSessionAverageSpeed / 10.0f;
	}

	void setCurrentSessionAverageSpeed(float currentSessionAverageSpeed)
	{
		int decupleCurrentSessionAverageSpeed = (int) (currentSessionAverageSpeed * 10);
		if (this.mDecupleCurrentSessionAverageSpeed != decupleCurrentSessionAverageSpeed)
		{
			this.mDecupleCurrentSessionAverageSpeed = decupleCurrentSessionAverageSpeed;

			if (mSportDataListener != null)
			{
				mSportDataListener.onCurrentSessionAverageSpeedChanged(currentSessionAverageSpeed);
			}
		}
	}

	void setCount(int count)
	{
		int decupleCount = count;
		if (this.mCount != decupleCount)
		{
			this.mCount = decupleCount;

			if (mSportDataListener != null)
			{
				mSportDataListener.onCountChanged(decupleCount);
			}
		}
	}

	public int getCount()
	{
		return mCount;
	}

	public interface DCSportDataListener
	{
		void onCurrentSpeedKmPerHourChanged(float currentSpeedKmPerHour);

		void onCurrentSessionCumulativeKCalChanged(int currentSessionCumulativeKCal);

		void onCurrentSessionCumulativeKMChanged(float currentSessionCumulativeKM);

		void onAnalogHeartRateChanged(int analogHeartRate);

		void onCurrentSessionAverageSpeedChanged(float currentSessionAverageSpeed);

		void onCountChanged(int count);
	}


}
