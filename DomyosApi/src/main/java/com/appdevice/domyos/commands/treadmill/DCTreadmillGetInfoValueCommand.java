package com.appdevice.domyos.commands.treadmill;

import com.appdevice.domyos.commands.DCGetInfoValueCommand;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DCTreadmillGetInfoValueCommand extends DCGetInfoValueCommand
{
	@Override
	protected HashMap<String, Object> processResponse(byte[] expectedResponse)
	{

		ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, expectedResponse.length - 3);

		int inclinePercentage = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		float targetInclinePercentage = 0;
		if (inclinePercentage >= 1 && inclinePercentage <= 1000)
		{
			targetInclinePercentage = -(inclinePercentage / 10.0f);
		}
		else if (inclinePercentage > 1000)
		{
			targetInclinePercentage = (inclinePercentage - 1000.0f) / 10.0f;
		}


		byteBuffer.position(6);

		float currentSpeedKmPerHour = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

		byteBuffer.position(8);

		int count = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		byteBuffer.position(10);

		int currentSessionCumulativeKCal = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		byteBuffer.position(12);

		float currentSessionCumulativeKM = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

		byteBuffer.position(15);

		int errorNumber = byteBuffer.get() & 0xFF;

		int tapOnEquipment = byteBuffer.get();

		int analogHeartRate = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		float currentSessionAverageSpeed = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

		int safetyMotorKeyPlugIn = byteBuffer.get() & 0xFF;

		int pressedButton = byteBuffer.get() & 0xFF;

		int fanSpeedLevel = byteBuffer.get() & 0xFF;

		int hotKeyStatus = byteBuffer.get() & 0xFF;

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("targetInclinePercentage", Float.valueOf(targetInclinePercentage));
		map.put("currentSpeedKmPerHour", Float.valueOf(currentSpeedKmPerHour));
		map.put("count", Integer.valueOf(count));
		map.put("currentSessionCumulativeKCal", Integer.valueOf(currentSessionCumulativeKCal));
		map.put("currentSessionCumulativeKM", Float.valueOf(currentSessionCumulativeKM));
		map.put("errorNumber", Integer.valueOf(errorNumber));
		map.put("tapOnEquipment", Integer.valueOf(tapOnEquipment));
		map.put("analogHeartRate", Integer.valueOf(analogHeartRate));
		map.put("currentSessionAverageSpeed", Float.valueOf(currentSessionAverageSpeed));
		map.put("safetyMotorKeyPlugIn", Integer.valueOf(safetyMotorKeyPlugIn));
		map.put("pressedButton", Integer.valueOf(pressedButton));
		map.put("fanSpeedLevel", Integer.valueOf(fanSpeedLevel));
		map.put("hotKeyStatus", Integer.valueOf(hotKeyStatus));

		return map;

	}
}
