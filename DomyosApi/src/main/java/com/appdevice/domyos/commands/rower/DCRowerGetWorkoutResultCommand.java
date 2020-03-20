package com.appdevice.domyos.commands.rower;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.commands.DCGetWorkoutResultCommand;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by apple on 2018/1/4.
 */

public class DCRowerGetWorkoutResultCommand extends DCGetWorkoutResultCommand
{
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

		int avgSpmHigh = byteBuffer.get() & 0xFF;
		int avgSpmLow = byteBuffer.get() & 0xFF;
		int avgSpm = (avgSpmHigh << 8) + avgSpmLow;

		int avgBpm = byteBuffer.get() & 0xFF;


		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("User", Integer.valueOf(user));
		map.put("TotalTimeInMinutes", Integer.valueOf(totalTimeInMinutes));
		map.put("TotalDistanceInKm", Float.valueOf(totalDistanceInKm));
		map.put("TotalCaloriesInKCal", Integer.valueOf(totalCaloriesInKCal));
		map.put("AvgSpm", Integer.valueOf(avgSpm));
		map.put("AvgBpm", Integer.valueOf(avgBpm));

		return map;
	}
}
