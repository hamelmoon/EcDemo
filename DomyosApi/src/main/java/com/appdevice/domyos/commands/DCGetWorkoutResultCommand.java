package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by apple on 2018/1/4.
 */

public class DCGetWorkoutResultCommand extends DCCommand
{
	int user;

	public DCGetWorkoutResultCommand()
	{
		user = 0xff;
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
		byteBuffer.put((byte) 0xE0);

		byteBuffer.put((byte) user);

		byte checksum = generateChecksum(byteBuffer.array());
		byteBuffer.put(checksum);

		return byteBuffer.array();
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
		if (uncheckedResponse[1] == (byte) 0xF0)
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
		ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, expectedResponse.length - 3);
		int user = byteBuffer.get() & 0xFF;

		int totalTimeInMinutesHigh = byteBuffer.get() & 0xFF;
		int totalTimeInMinutesLow = byteBuffer.get() & 0xFF;
		int totalTimeInMinutes = (totalTimeInMinutesHigh << 8) + totalTimeInMinutesLow;

		int totalDistanceInKmHigh = byteBuffer.get() & 0xFF;
		int totalDistanceInKmLow = byteBuffer.get() & 0xFF;
		float totalDistanceInKm = ((totalDistanceInKmHigh << 8) + totalDistanceInKmLow) / 10.0f;

		int totalCaloriesInKCalHigh = byteBuffer.get() & 0xFF;
		int totalCaloriesInKCalLow = byteBuffer.get() & 0xFF;
		int totalCaloriesInKCal = (totalCaloriesInKCalHigh << 8) + totalCaloriesInKCalLow;

		int avgSpeedHigh = byteBuffer.get() & 0xFF;
		int avgSpeedLow = byteBuffer.get() & 0xFF;
		float avgSpeed = ((avgSpeedHigh << 8) + avgSpeedLow) / 10.0f;

		int avgBpm = byteBuffer.get() & 0xFF;


		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("User", Integer.valueOf(user));
		map.put("TotalTimeInMinutes", Integer.valueOf(totalTimeInMinutes));
		map.put("TotalDistanceInKm", Float.valueOf(totalDistanceInKm));
		map.put("TotalCaloriesInKCal", Integer.valueOf(totalCaloriesInKCal));
		map.put("AvgSpeed", Float.valueOf(avgSpeed));
		map.put("AvgBpm", Integer.valueOf(avgBpm));

		return map;
	}

	@Override
	protected DCError getParameterError()
	{
		if ((user >= 1 && user <= 10) || user == 0xFF)
		{
			return null;
		}
		else
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, String.format("user number out of range (%d not between 1 and 10)", user));
			return error;
		}
	}

	public int getUser()
	{
		return user;
	}

	public void setUser(int user)
	{
		this.user = user;
	}
}
