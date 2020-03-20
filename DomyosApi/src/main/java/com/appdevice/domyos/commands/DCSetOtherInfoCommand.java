package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

class DCSetOtherInfoCommand extends DCCommand
{
	int mCount1;
	int mCount2;
	int mCount3;
	int mWeight1;
	int mWeight2;

	DCSetOtherInfoCommand()
	{
		mCount1 = 0xFFFF;
		mCount2 = 0xFFFF;
		mCount3 = 0xFFFF;
		mWeight1 = 0xFFFF;
		mWeight2 = 0xFFFF;
	}

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(13);
		byteBuffer.put((byte) 0xF0);
		byteBuffer.put((byte) 0xC0);
		byteBuffer.put((byte) (mCount1 / 0x100));
		byteBuffer.put((byte) (mCount1 % 0x100));
		byteBuffer.put((byte) (mCount2 / 0x100));
		byteBuffer.put((byte) (mCount2 % 0x100));
		byteBuffer.put((byte) (mCount3 / 0x100));
		byteBuffer.put((byte) (mCount3 % 0x100));
		byteBuffer.put((byte) (mWeight1 / 0x100));
		byteBuffer.put((byte) (mWeight1 % 0x100));
		byteBuffer.put((byte) (mWeight2 / 0x100));
		byteBuffer.put((byte) (mWeight2 % 0x100));

		byte checksum = generateChecksum(byteBuffer.array());
		byteBuffer.put(checksum);

		return byteBuffer.array();
	}

	@Override
	protected DCError getParameterError()
	{
		int[] values = { mCount1, mCount2, mCount3, mWeight1, mWeight2 };
		String[] descriptions = { "count1", "count2", "count3", "weight1", "weight2" };

		for (int i = 0; i < 5; i++)
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
		return 13;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xD0)
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
