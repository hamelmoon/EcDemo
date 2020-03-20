package com.appdevice.domyos;

/**
 * Created by apple on 2017/5/1.
 */

public class DCRowerSportData extends DCSportData
{
	int mTimePer500mInSeconds;
	int mCurrentSPM;
	int mTorqueResistanceLevel;
	int mWatt;

	public void setListener(DCRowerSportData.DCRowerSportDataListener rowerSportDataListener)
	{
		this.mSportDataListener = rowerSportDataListener;
	}

	public int getTimePer500mInSeconds()
	{
		return mTimePer500mInSeconds;
	}

	public void setTimePer500mInSeconds(int timePer500mInSeconds)
	{
		if (this.mTimePer500mInSeconds != timePer500mInSeconds)
		{
			mTimePer500mInSeconds = timePer500mInSeconds;

			if (mSportDataListener != null)
			{
				((DCRowerSportData.DCRowerSportDataListener) mSportDataListener).onTimePer500mChanged(timePer500mInSeconds);
			}
		}
	}

	public int getWatt()
	{
		return mWatt;
	}

	void setWatt(int watt)
	{
		if (this.mWatt != watt)
		{
			this.mWatt = watt;

			if (mSportDataListener != null)
			{
				((DCRowerSportData.DCRowerSportDataListener) mSportDataListener).onWattChanged(watt);
			}
		}
	}

	public int getCurrentSPM()
	{
		return mCurrentSPM;
	}

	void setCurrentSPM(int currentSPM)
	{
		if (this.mCurrentSPM != currentSPM)
		{
			this.mCurrentSPM = currentSPM;

			if (mSportDataListener != null)
			{
				((DCRowerSportData.DCRowerSportDataListener) mSportDataListener).onCurrentSPMChanged(currentSPM);
			}
		}
	}

	public int getTorqueResistanceLevel()
	{
		return mTorqueResistanceLevel;
	}

	void setTorqueResistanceLevel(int torqueResistanceLevel)
	{
		if (this.mTorqueResistanceLevel != torqueResistanceLevel)
		{
			this.mTorqueResistanceLevel = torqueResistanceLevel;

			if (mSportDataListener != null)
			{
				((DCRowerSportData.DCRowerSportDataListener) mSportDataListener).onTorqueResistanceLevelChanged(torqueResistanceLevel);
			}
		}
	}

	@Override
	public float getCurrentSpeedKmPerHour()
	{
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	void setCurrentSpeedKmPerHour(float currentSpeedKmPerHour)
	{
		throw new UnsupportedOperationException("not supported");
	}

	public interface DCRowerSportDataListener extends DCSportDataListener
	{
		void onWattChanged(float watt);

		void onCurrentSPMChanged(int currentSPM);

		void onTorqueResistanceLevelChanged(int torqueResistanceLevel);

		void onTimePer500mChanged(int timePer500mInSeconds);
	}
}
