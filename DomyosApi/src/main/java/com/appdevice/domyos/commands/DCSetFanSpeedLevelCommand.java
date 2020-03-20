package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCSetFanSpeedLevelCommand extends DCCommand
{

	public byte mFanSpeedLevel;

	public DCSetFanSpeedLevelCommand()
	{
		mFanSpeedLevel = (byte) 0xFF;
	}

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.put((byte) 0xF0);
		byteBuffer.put((byte) 0xCA);
		byteBuffer.put(mFanSpeedLevel);

		byte checksum = generateChecksum(byteBuffer.array());
		byteBuffer.put(checksum);

		return byteBuffer.array();
	}

	@Override
	protected DCError getParameterError()
	{
		if ((mFanSpeedLevel >= 0 && mFanSpeedLevel <=5) || mFanSpeedLevel == 0xFF)
		{
			return null;
		}
		else
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, String.format("fanSpeedLevel out of range (%d not between 0 and 5)", mFanSpeedLevel));
			return error;
		}
	}

	@Override
	protected byte getExpectedResponseFirstValue()
	{
		return (byte) 0xF0;
	}

	@Override
	protected int getExpectedResponseLength()
	{
		return 4;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xDA)
		{

			byte[] data = Arrays.copyOf(uncheckedResponse, uncheckedResponse.length - 1);
			byte checksum = generateChecksum(data);
			byte receiveChecksum = uncheckedResponse[uncheckedResponse.length - 1];
			if (checksum == receiveChecksum)
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

}
