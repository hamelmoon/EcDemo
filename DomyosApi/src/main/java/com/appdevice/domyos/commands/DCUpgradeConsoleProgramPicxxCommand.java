package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.util.HashMap;

public class DCUpgradeConsoleProgramPicxxCommand extends DCCommand
{

	private final byte[] mCommandData;

	public DCUpgradeConsoleProgramPicxxCommand(byte[] commandData)
	{
		mCommandData = commandData;
	}

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting;
	}

	@Override
	protected byte[] getRequestData()
	{
		return generateConsoleUpdateCommand(mCommandData);
	}

	@Override
	protected int getRetryCount()
	{
		return 2;
	}

	@Override
	protected float getRetryTimeout()
	{
		return 3.0f;
	}

	@Override
	protected byte getExpectedResponseFirstValue()
	{
		return (byte) 0x55;
	}

	@Override
	protected int getExpectedResponseLength()
	{
		return 0;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse.length >= 5 && verifyConsoleUpdateCommand(uncheckedResponse))
		{
			return true;
		}
		return false;
	}

	@Override
	protected HashMap<String, Object> processResponse(byte[] expectedResponse)
	{
		return null;
	}

	@Override
	protected DCError getParameterError()
	{
		return null;
	}
}
