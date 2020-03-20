package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by apple on 2016/12/6.
 */

public class DCSetDisplayZoneOffSecondAreaCommand extends DCCommand
{
	public int displayZone7OffParameter;
	public int displayZone8OffParameter;
	public int displayZone9OffParameter;
	public int displayZone10OffParameter;
	public int displayZone11OffParameter;
	public int displayZone12OffParameter;

	public DCSetDisplayZoneOffSecondAreaCommand()
	{
		displayZone7OffParameter = 0xFF;
		displayZone8OffParameter = 0xFF;
		displayZone9OffParameter = 0xFF;
		displayZone10OffParameter = 0xFF;
		displayZone11OffParameter = 0xFF;
		displayZone12OffParameter = 0xFF;
	}

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(9);
		byteBuffer.put((byte) 0xF0);
		byteBuffer.put((byte) 0xCE);
		byteBuffer.put((byte) (displayZone7OffParameter & 0xff));
		byteBuffer.put((byte) (displayZone8OffParameter & 0xff));
		byteBuffer.put((byte) (displayZone9OffParameter & 0xff));
		byteBuffer.put((byte) (displayZone10OffParameter & 0xff));
		byteBuffer.put((byte) (displayZone11OffParameter & 0xff));
		byteBuffer.put((byte) (displayZone12OffParameter & 0xff));

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
		return 9;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xDE)
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

	@Override
	protected DCError getParameterError()
	{
		return null;
	}
}
