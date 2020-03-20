package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

class DCSetRepetitionCommand extends DCCommand
{
	int mRepetition1;
	int mRepetition2;

	DCSetRepetitionCommand()
	{
		mRepetition1 = 0xFFFF;
		mRepetition2 = 0xFFFF;
	}

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(7);
		byteBuffer.put((byte) 0xF0);
		byteBuffer.put((byte) 0xC2);
		byteBuffer.put((byte) (mRepetition1 / 0x100));
		byteBuffer.put((byte) (mRepetition2 % 0x100));

		byte checksum = generateChecksum(byteBuffer.array());
		byteBuffer.put(checksum);

		return byteBuffer.array();
	}

	@Override
	protected DCError getParameterError()
	{
		int[] values = { mRepetition1, mRepetition2 };
		String[] descriptions = { "repetition1", "repetition2" };

		for (int i = 0; i < 2; i++)
		{
			int value = values[i];
			if (value != 0xFFFF && (value < 0 || value > 1000))
			{
				DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "%s out of range (%d not between 0 and 1000)", descriptions[i], value);
				return error;
			}

		}
		return null;
	}

	@Override
	protected byte getExpectedResponseFirstValue()
	{
		return (byte) 0xF0;
	}

	@Override
	protected int getExpectedResponseLength()
	{
		return 7;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xD2)
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
