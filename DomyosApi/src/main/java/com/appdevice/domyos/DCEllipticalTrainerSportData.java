package com.appdevice.domyos;

public class DCEllipticalTrainerSportData extends DCSportData
{
	private int mWatt;

	private int mCurrentRPM;

	private int mTorqueResistanceLevel;

    public int getmIncline() {
        return mIncline;
    }

    public void setmIncline(int mIncline) {
        if (this.mIncline != mIncline) {
            this.mIncline = mIncline;

            if (mSportDataListener != null) {
                ((DCEllipticalTrainerSportDataListener) mSportDataListener).onInclineChanged(mIncline);
            }
        }
    }

    private int mIncline;

    public void setListener(DCEllipticalTrainerSportDataListener ellipticalTrainerSportDataListener) {
        this.mSportDataListener = ellipticalTrainerSportDataListener;
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
				((DCEllipticalTrainerSportDataListener) mSportDataListener).onWattChanged(watt);
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
				((DCEllipticalTrainerSportDataListener) mSportDataListener).onCurrentRPMChanged(currentRPM);
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
				((DCEllipticalTrainerSportDataListener) mSportDataListener).onTorqueResistanceLevelChanged(torqueResistanceLevel);
			}
		}
	}

	public interface DCEllipticalTrainerSportDataListener extends DCSportDataListener
	{
		void onWattChanged(float watt);

		void onCurrentRPMChanged(int currentRPM);

        void onTorqueResistanceLevelChanged(int torqueResistanceLevel);

        void onInclineChanged(int incline);
    }

}
