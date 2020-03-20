package com.appdevice.domyos;


public class DCBikeSportData extends DCSportData
{
	private int mWatt;

	private int mCurrentRPM;

	private int mTorqueResistanceLevel;

	public void setListener(DCBikeSportDataListener bikeSportDataListener)
	{
		this.mSportDataListener = bikeSportDataListener;
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
				((DCBikeSportDataListener) mSportDataListener).onWattChanged(watt);
			}
		}
	}

	public int getCurrentRPM()
	{
		return mCurrentRPM;
	}

	void setCurrentRPM(int currentRPM)
	{
		if (this.mCurrentRPM != currentRPM)
		{
			this.mCurrentRPM = currentRPM;

			if (mSportDataListener != null)
			{
				((DCBikeSportDataListener) mSportDataListener).onCurrentRPMChanged(currentRPM);
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
				((DCBikeSportDataListener) mSportDataListener).onTorqueResistanceLevelChanged(torqueResistanceLevel);
			}
		}
	}

	public interface DCBikeSportDataListener extends DCSportDataListener
	{
		void onWattChanged(float watt);

		void onCurrentRPMChanged(int currentRPM);

		void onTorqueResistanceLevelChanged(int torqueResistanceLevel);
	}

}
