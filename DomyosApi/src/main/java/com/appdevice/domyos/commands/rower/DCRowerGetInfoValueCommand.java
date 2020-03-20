package com.appdevice.domyos.commands.rower;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.commands.DCGetInfoValueCommand;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by apple on 2017/5/3.
 */

public class DCRowerGetInfoValueCommand extends DCGetInfoValueCommand
{
	@Override
	protected HashMap<String, Object> processResponse(byte[] expectedResponse)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, expectedResponse.length - 3);

		byteBuffer.position(2);

		int count = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		byteBuffer.position(4);

		int watt = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		int timePer500mInSeconds = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		int currentSPM = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		int currentSessionCumulativeKCal = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		byteBuffer.position(12);

		float currentSessionCumulativeKM = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

		int torqueResistanceLevel = byteBuffer.get() & 0xFF;

		int errorNumber = byteBuffer.get() & 0xFF;

		int tapOnEquipment = byteBuffer.get() & 0xFF;

		int analogHeartRate = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

		float currentSessionAverageSpeed = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

		byteBuffer.position(22);

		int pressedButton = byteBuffer.get() & 0xff;

		int fanSpeedLevel = byteBuffer.get() & 0xff;

		int hotKeyStatus = byteBuffer.get() & 0xff;

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("watt", Integer.valueOf(watt));
		map.put("timePer500mInSeconds", Integer.valueOf(timePer500mInSeconds));
		map.put("currentSPM", Integer.valueOf(currentSPM));
		map.put("count", Integer.valueOf(count));
		map.put("currentSessionCumulativeKCal", Integer.valueOf(currentSessionCumulativeKCal));
		map.put("currentSessionCumulativeKM", Float.valueOf(currentSessionCumulativeKM));
		map.put("torqueResistanceLevel", Integer.valueOf(torqueResistanceLevel));
		map.put("errorNumber", Integer.valueOf(errorNumber));
		map.put("tapOnEquipment", Integer.valueOf(tapOnEquipment));
		map.put("analogHeartRate", Integer.valueOf(analogHeartRate));
		map.put("currentSessionAverageSpeed", Float.valueOf(currentSessionAverageSpeed));
		map.put("pressedButton", Integer.valueOf(pressedButton));
		map.put("fanSpeedLevel", Integer.valueOf(fanSpeedLevel));
		map.put("hotKeyStatus", Integer.valueOf(hotKeyStatus));

		return map;
	}
}
