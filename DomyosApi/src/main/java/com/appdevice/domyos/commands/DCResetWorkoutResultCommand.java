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

public class DCResetWorkoutResultCommand extends DCCommand
{
	int user;

	public DCResetWorkoutResultCommand()
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
		byteBuffer.put((byte) 0xE1);

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
		return 4;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xF1)
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
