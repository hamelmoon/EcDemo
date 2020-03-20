package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DCUpgradeConsoleEraseCommand extends DCCommand
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
		byteBuffer.put((byte) 0x03);
		byteBuffer.put((byte) 0x55);
		byteBuffer.put((byte) 0x00);
		byteBuffer.put((byte) 0x00);
		byteBuffer.put((byte) 0x00);

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
		return 5.0f;
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
