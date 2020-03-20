package com.appdevice.domyos.parameters;

public class DCSetInfoParameters 
{
    public int mCurrentSpeedKmPerHour;
    public int mTorqueResistanceLevel;
    public int mTargetInclinePercentage;
    public int mWatt;
    public int mHeartRateLedColor;
    public int mBtLedSwitch;
    public int mIncline;

    public DCSetInfoParameters() 
	{
        mCurrentSpeedKmPerHour = 0xFFFF;
        mTorqueResistanceLevel = 0xFF;
        mTargetInclinePercentage = 0xFFFF;
        mWatt = 0xFFFF;
        mHeartRateLedColor = 0xFF;
        mBtLedSwitch = 0xFF;
        mIncline = 0xFF;
    }

}
