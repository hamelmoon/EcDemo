package com.appdevice.domyos.commands.treadmill;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCGetTreadmillDataCommand extends DCCommand
{

	@Override
	protected int getCompatibilityModes()
	{

		return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
		byteBuffer.put((byte) 0xF0);
		byteBuffer.put((byte) 0xA7);

		byte checksum = generateChecksum(byteBuffer.array());
		byteBuffer.put(checksum);

		return byteBuffer.array();
	}

	@Override
	protected DCError getParameterError()
	{
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
		return 16;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xB7)
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

		int calibrationStatus = byteBuffer.get() & 0xFF;

		float motorSpeed = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

		float motorVoltage = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF)) / 6.504f;

		float motorCurrent = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF)) / 95.57f;

		float inclineCalibrationMax = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF)) / 819.2f;

		float inclineCalibrationMin = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF)) / 819.2f;

		int temperatureMappingValue = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF));

		int temperatureMappingValues[] = new int[] { 0x30B, 0x302, 0x2F9, 0x2EF, 0x2E5, 0x2DC, 0x2D1, 0x2C7, 0x2BC, 0x2B2, 0x2A7, 0x29C, 0x291, 0x287, 0x27C, 0x271, 0x265, 0x25A, 0x24F, 0x243, 0x238, 0x22D, 0x221, 0x216, 0x20B, 0x200, 0x1F4, 0x1E9, 0x1DF, 0x1D4, 0x1C9, 0x1BE, 0x1B4, 0x1A9, 0x19F, 0x194, 0x18A, 0x180, 0x177, 0x16D, 0x164, 0x15B, 0x151, 0x148, 0x140, 0x137, 0x12F, 0x126, 0x11E, 0x116, 0x10E, 0x107, 0xFF, 0xF8, 0xF1, 0xEA, 0xE4, 0xDD, 0xD7, 0xD1, 0xCB, 0xC5, 0xC0, 0xBA, 0xB5, 0xB0, 0xAB, 0xA6, 0xA1, 0x9C, 0x98, 0x94, 0x8F, 0x8B, 0x87, 0x84, 0x80, 0x7C, 0x78, 0x75, 0x71, 0x6E, 0x6B, 0x68, 0x65, 0x62, 0x5F, 0x5D, 0x5A, 0x57, 0x55, 0x53, 0x50, 0x4E, 0x4C, 0x4A, 0x48, 0x46, 0x44, 0x42, 0x40, 0x3E, 0x3D, 0x3B, 0x3A, 0x38, 0x36, 0x35, 0x33, 0x32, 0x31, 0x2F, 0x2E, 0x2D, 0x2C, 0x2B, 0x2A, 0x28, 0x27, 0x26, 0x26, 0x25, 0x24, 0x23, 0x22, 0x21 };

		int temperature = 0;
		for (int i = 0; i < 126; i++)
		{
			if (temperatureMappingValue > 0x30B || temperatureMappingValue < 0x21)
			{
				temperature = 0;
				break;
			}
			if (temperatureMappingValue > temperatureMappingValues[i])
			{
				temperature = i - 1;
				break;
			}
			else if (temperatureMappingValue == temperatureMappingValues[i])
			{
				temperature = i;
				break;
			}
		}

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("calibrationStatus", Integer.valueOf(calibrationStatus));
		map.put("motorSpeed", Float.valueOf(motorSpeed));
		map.put("motorVoltage", Float.valueOf(motorVoltage));
		map.put("motorCurrent", Float.valueOf(motorCurrent));
		map.put("inclineCalibrationMax", Float.valueOf(inclineCalibrationMax));
		map.put("inclineCalibrationMin", Float.valueOf(inclineCalibrationMin));
		map.put("temperature", Integer.valueOf(temperature));

		return map;
	}

}
