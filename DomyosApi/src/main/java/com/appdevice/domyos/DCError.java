package com.appdevice.domyos;

public class DCError
{
	private final int mCode;
	private final String mDescription;

	public DCError(int code, String descriptionFormat, Object... descriptionArgs)
	{
		mCode = code;
		mDescription = String.format(descriptionFormat, descriptionArgs);
	}

	public int getCode()
	{
		return mCode;
	}

	public String getDescription()
	{
		return mDescription;
	}
}
