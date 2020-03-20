package com.appdevice.domyos.parameters.treadmill;

public class DCTreadmillWorkoutModeSetInfoParameters extends DCWorkoutModeSetInfoParameters
{
	public void setCurrentSpeedKmPerHour(float currentSpeedKmPerHour)
	{
		this.mCurrentSpeedKmPerHour = (int) (currentSpeedKmPerHour * 10);
	}

	public void setTargetInclinePercentage(float targetInclinePercentage)
	{
		if (targetInclinePercentage == 0)
		{
			this.mTargetInclinePercentage = 0;
		}
		else if (targetInclinePercentage > 0)
		{
			this.mTargetInclinePercentage = (int) (1000 + targetInclinePercentage * 10);
		}
		else
		{
			this.mTargetInclinePercentage = (int) ((targetInclinePercentage * -1) * 10);
		}
	}

}
