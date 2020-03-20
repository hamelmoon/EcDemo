package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCGetSerialNumberCommand extends DCCommand
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
		byteBuffer.put((byte) 0xA4);

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
		if (uncheckedResponse[1] == (byte) 0xB4)
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
		int consoleYear = byteBuffer.get() & 0xff;
		int consoleWeek = byteBuffer.get() & 0xff;
		
		int consoleSubSerial = ((byteBuffer.get() & 0xff) << 16 | (byteBuffer.get() & 0xff) << 8 | (byteBuffer.get() & 0xff));
		
		String consoleFirmwareSerialNumber = String.format("%02d%02d%05d",consoleYear,consoleWeek,consoleSubSerial);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("consoleFirmwareSerialNumber", consoleFirmwareSerialNumber);

		return map;
	}

	@Override
	protected DCError getParameterError()
	{
		return null;
	}

}
