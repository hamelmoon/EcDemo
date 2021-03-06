package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DCUpgradeConsoleGetIDCommand extends DCCommand
{

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(5);
		byteBuffer.put((byte) 0x01);
		byteBuffer.put((byte) 0x01);
		byteBuffer.put((byte) 0x00);
		byteBuffer.put((byte) 0x00);
		byteBuffer.put((byte) 0xFF);

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
		return 13;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[2] == (byte) 0x01 && uncheckedResponse[3] == (byte) 0x01 && 
		uncheckedResponse[4] == (byte) 0x00 && uncheckedResponse[5] == (byte) 0x00 && 
		uncheckedResponse[6] == (byte) 0xff && uncheckedResponse[7] == (byte) 0x08 && 
		uncheckedResponse[8] == (byte) 0x10 && uncheckedResponse[9] == (byte) 0x00 && 
		uncheckedResponse[10] == (byte) 0x00)
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
