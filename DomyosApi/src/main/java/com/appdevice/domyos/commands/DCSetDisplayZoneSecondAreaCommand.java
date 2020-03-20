package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.parameters.DCDisplayZoneParameter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by apple on 2016/12/6.
 */

public class DCSetDisplayZoneSecondAreaCommand extends DCCommand
{
	public DCDisplayZoneParameter displayZone7Parameter;
	public DCDisplayZoneParameter displayZone8Parameter;
	public DCDisplayZoneParameter displayZone9Parameter;
	public DCDisplayZoneParameter displayZone10Parameter;
	public DCDisplayZoneParameter displayZone11Parameter;
	public DCDisplayZoneParameter displayZone12Parameter;

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(27);
		byteBuffer.put((byte) 0xF0);
		byteBuffer.put((byte) 0xCD);

		byteBuffer.put((byte) displayZone7Parameter.zoneType);
		byteBuffer.put((byte) (displayZone7Parameter.zoneVariable1 / 256));
		byteBuffer.put((byte) (displayZone7Parameter.zoneVariable1 % 256));
		byteBuffer.put((byte) displayZone7Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone8Parameter.zoneType);
		byteBuffer.put((byte) (displayZone8Parameter.zoneVariable1 / 256));
		byteBuffer.put((byte) (displayZone8Parameter.zoneVariable1 % 256));
		byteBuffer.put((byte) displayZone8Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone9Parameter.zoneType);
		byteBuffer.put((byte) (displayZone9Parameter.zoneVariable1 / 256));
		byteBuffer.put((byte) (displayZone9Parameter.zoneVariable1 % 256));
		byteBuffer.put((byte) displayZone9Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone10Parameter.zoneType);
		byteBuffer.put((byte) (displayZone10Parameter.zoneVariable1 / 256));
		byteBuffer.put((byte) (displayZone10Parameter.zoneVariable1 % 256));
		byteBuffer.put((byte) displayZone10Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone11Parameter.zoneType);
		byteBuffer.put((byte) (displayZone11Parameter.zoneVariable1 / 256));
		byteBuffer.put((byte) (displayZone11Parameter.zoneVariable1 % 256));
		byteBuffer.put((byte) displayZone11Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone12Parameter.zoneType);
		byteBuffer.put((byte) (displayZone12Parameter.zoneVariable1 / 256));
		byteBuffer.put((byte) (displayZone12Parameter.zoneVariable1 % 256));
		byteBuffer.put((byte) displayZone12Parameter.zoneVariable2);

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
		return 27;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xDD)
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
		if (displayZone7Parameter == null)
		{
			displayZone7Parameter = new DCDisplayZoneParameter();
		}
		if (displayZone8Parameter == null)
		{
			displayZone8Parameter = new DCDisplayZoneParameter();
		}
		if (displayZone9Parameter == null)
		{
			displayZone9Parameter = new DCDisplayZoneParameter();
		}
		if (displayZone10Parameter == null)
		{
			displayZone10Parameter = new DCDisplayZoneParameter();
		}
		if (displayZone11Parameter == null)
		{
			displayZone11Parameter = new DCDisplayZoneParameter();
		}
		if (displayZone12Parameter == null)
		{
			displayZone12Parameter = new DCDisplayZoneParameter();
		}

		List<DCDisplayZoneParameter> params = new ArrayList<>();
		params.add(displayZone7Parameter);
		params.add(displayZone8Parameter);
		params.add(displayZone9Parameter);
		params.add(displayZone10Parameter);
		params.add(displayZone11Parameter);
		params.add(displayZone12Parameter);


		DCError error = null;
		int paramIndex = 7;
		for (DCDisplayZoneParameter param : params)
		{
			error = checkDisplayZoneParameter(param, paramIndex);
			if (error != null)
				return error;

			paramIndex++;
		}

		return null;
	}

	private DCError checkDisplayZoneParameter(DCDisplayZoneParameter displayZoneParameter, int paramIndex)
	{
		if (displayZoneParameter == null)
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, String.format("Please set the displayZone%dParameter", paramIndex));
			return error;
		}

		if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeArithmetic)
		{
			if (displayZoneParameter.zoneVariable1 < 0 || displayZoneParameter.zoneVariable1 > 999)
			{
				if (displayZoneParameter.zoneVariable2 == 0)
				{
					DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, String.format("displayZone%dParameter out of range (%d not between 0 and 999)", paramIndex, displayZoneParameter.zoneVariable1));
					return error;
				}
				else if (displayZoneParameter.zoneVariable2 == 1)
				{
					DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, String.format("displayZone%dParameter out of range (%.01f not between 0.0 and 99.9)", paramIndex, displayZoneParameter.zoneVariable1 * 0.1f));
					return error;
				}
				else if (displayZoneParameter.zoneVariable2 == 2)
				{
					DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, String.format("displayZone%dParameter out of range (%.02f not between 0.00 and 9.99)", paramIndex, displayZoneParameter.zoneVariable1 * 0.1f));
					return error;
				}
			}
		}
		else if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeClcok)
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, String.format("displayZone%dParameter doesn't allow Clcok Type", paramIndex));
			return error;
		}
		else if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeString)
		{
			if (displayZoneParameter.zoneVariable1 < 1 || displayZoneParameter.zoneVariable1 > 12 || displayZoneParameter.zoneVariable1 == 3 || displayZoneParameter.zoneVariable1 == 9 || displayZoneParameter.zoneVariable1 == 10)
			{
				DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, String.format("displayZone%dParameter out of range (%d)", paramIndex, displayZoneParameter.zoneVariable1));
				return error;
			}
		}

		return null;

	}
}
