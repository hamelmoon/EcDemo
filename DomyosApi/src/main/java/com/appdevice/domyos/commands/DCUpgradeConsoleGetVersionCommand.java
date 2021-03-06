package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DCUpgradeConsoleGetVersionCommand extends DCCommand
{

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(2);
		byteBuffer.put((byte) 0x00);
		byteBuffer.put((byte) 0x02);

		return generateConsoleUpdateCommand(byteBuffer.array());
	}

	@Override
	protected int getRetryCount()
	{
		return 3;
	}

	@Override
	protected float getRetryTimeout()
	{
		return 2.0f;
	}

	@Override
	protected byte getExpectedResponseFirstValue()
	{
		return (byte) 0x55;
	}

	@Override
	protected int getExpectedResponseLength()
	{
		return 8;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[2] == (byte) 0x00 && uncheckedResponse[3] == (byte) 0x02)
		{
			if (verifyConsoleUpdateCommand(uncheckedResponse))
			{
				return true;
			}
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
