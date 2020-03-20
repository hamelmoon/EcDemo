package com.appdevice.domyos;

public class DCTreadmillSportData extends DCSportData
{
	float mTargetInclinePercentage;

	public void setListener(DCTreadmillSportDataListener treadmillSportDataListener)
	{
		this.mSportDataListener = treadmillSportDataListener;
	}

	public float getTargetInclinePercentage()
	{
		return mTargetInclinePercentage;
	}

	void setTargetInclinePercentage(float targetInclinePercentage)
	{

		if (this.mTargetInclinePercentage != targetInclinePercentage)
		{
			this.mTargetInclinePercentage = targetInclinePercentage;

			if (mSportDataListener != null)
			{
				((DCTreadmillSportDataListener) mSportDataListener).onTargetInclinePercentageChanged(targetInclinePercentage);
			}
		}
	}

	public interface DCTreadmillSportDataListener extends DCSportDataListener
	{
		void onTargetInclinePercentageChanged(float targetInclinePercentage);
	}

}
