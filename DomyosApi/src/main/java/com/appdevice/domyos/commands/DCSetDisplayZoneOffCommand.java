package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.parameters.DCDisplayZoneOffParameters;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCSetDisplayZoneOffCommand extends DCCommand
{
	public DCDisplayZoneOffParameters mDisplayZoneOffParameters;

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
		byteBuffer.put((byte) 0xCC);
		byteBuffer.put(mDisplayZoneOffParameters.mDisplayZone1OffParameter);
		byteBuffer.put(mDisplayZoneOffParameters.mDisplayZone2OffParameter);
		byteBuffer.put(mDisplayZoneOffParameters.mDisplayZone3OffParameter);
		byteBuffer.put(mDisplayZoneOffParameters.mDisplayZone4OffParameter);
		byteBuffer.put(mDisplayZoneOffParameters.mDisplayZone5OffParameter);
		byteBuffer.put(mDisplayZoneOffParameters.mDisplayZone6OffParameter);

		byte checksum = generateChecksum(byteBuffer.array());
		byteBuffer.put(checksum);

		return byteBuffer.array();
	}

	@Override
	protected DCError getParameterError()
	{
		if (mDisplayZoneOffParameters == null)
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "Please set the parameters");
			return error;
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
		return 9;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xDC)
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
