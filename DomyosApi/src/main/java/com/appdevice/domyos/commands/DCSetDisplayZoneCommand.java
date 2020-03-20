package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.parameters.DCDisplayZoneParameter;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCSetDisplayZoneCommand extends DCCommand
{
	public DCDisplayZoneParameter displayZone1Parameter;
	public DCDisplayZoneParameter displayZone2Parameter;
	public DCDisplayZoneParameter displayZone3Parameter;
	public DCDisplayZoneParameter displayZone4Parameter;
	public DCDisplayZoneParameter displayZone5Parameter;
	public DCDisplayZoneParameter displayZone6Parameter;

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
		byteBuffer.put((byte) 0xCB);

		byteBuffer.put((byte) displayZone1Parameter.zoneType);
		byteBuffer.put((byte) (displayZone1Parameter.zoneVariable1 / 0x100));
		byteBuffer.put((byte) (displayZone1Parameter.zoneVariable1 % 0x100));
		byteBuffer.put((byte) displayZone1Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone2Parameter.zoneType);
		byteBuffer.put((byte) (displayZone2Parameter.zoneVariable1 / 0x100));
		byteBuffer.put((byte) (displayZone2Parameter.zoneVariable1 % 0x100));
		byteBuffer.put((byte) displayZone2Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone3Parameter.zoneType);
		byteBuffer.put((byte) (displayZone3Parameter.zoneVariable1 / 0x100));
		byteBuffer.put((byte) (displayZone3Parameter.zoneVariable1 % 0x100));
		byteBuffer.put((byte) displayZone3Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone4Parameter.zoneType);
		byteBuffer.put((byte) (displayZone4Parameter.zoneVariable1 / 0x100));
		byteBuffer.put((byte) (displayZone4Parameter.zoneVariable1 % 0x100));
		byteBuffer.put((byte) displayZone4Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone5Parameter.zoneType);
		byteBuffer.put((byte) (displayZone5Parameter.zoneVariable1 / 0x100));
		byteBuffer.put((byte) (displayZone5Parameter.zoneVariable1 % 0x100));
		byteBuffer.put((byte) displayZone5Parameter.zoneVariable2);

		byteBuffer.put((byte) displayZone6Parameter.zoneType);
		byteBuffer.put((byte) (displayZone6Parameter.zoneVariable1 / 0x100));
		byteBuffer.put((byte) (displayZone6Parameter.zoneVariable1 % 0x100));
		byteBuffer.put((byte) displayZone6Parameter.zoneVariable2);

		byte checksum = generateChecksum(byteBuffer.array());
		byteBuffer.put(checksum);

		return byteBuffer.array();
	}

	@Override
	protected DCError getParameterError()
	{
		if (displayZone1Parameter == null)
		{
			displayZone1Parameter = new DCDisplayZoneParameter();;
		}
		if (displayZone2Parameter == null)
		{
			displayZone2Parameter = new DCDisplayZoneParameter();;
		}
		if (displayZone3Parameter == null)
		{
			displayZone3Parameter = new DCDisplayZoneParameter();;
		}
		if (displayZone4Parameter == null)
		{
			displayZone4Parameter = new DCDisplayZoneParameter();;
		}
		if (displayZone5Parameter == null)
		{
			displayZone5Parameter = new DCDisplayZoneParameter();;
		}
		if (displayZone6Parameter == null)
		{
			displayZone6Parameter = new DCDisplayZoneParameter();;
		}

		DCDisplayZoneParameter[] displayZoneParameters = { displayZone1Parameter, displayZone2Parameter, displayZone3Parameter, displayZone4Parameter, displayZone5Parameter, displayZone6Parameter };

		DCError error = null;
		DCDisplayZoneParameter displayZoneParameter = null;
		for (int i = 0; i < displayZoneParameters.length; i++)
		{
			displayZoneParameter = displayZoneParameters[i];
			error = checkDisplayZoneParameter(displayZoneParameter, i);
			if (error != null)
			{
				return error;
			}
		}
		return null;
	}

	private DCError checkDisplayZoneParameter(DCDisplayZoneParameter displayZoneParameter, int index)
	{
		if (displayZoneParameter == null)
		{
			return new DCError(DCEquipment.DCErrorCodeWrongParameter, "Please set the displayZone%dParameter", index + 1);
		}

		if (index == 0)
		{
			if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeArithmetic)
			{
				if (displayZoneParameter.zoneVariable2 == 1)
				{
					return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter doesn't allow one decimal place", index + 1);
				}

				if (displayZoneParameter.zoneVariable1 < 0 || displayZoneParameter.zoneVariable1 > 9999)
				{
					if (displayZoneParameter.zoneVariable2 == 0)
					{

						return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter out of range (%d not between 0 and 9999)", index + 1, displayZoneParameter.zoneVariable1);
					}
					else if (displayZoneParameter.zoneVariable2 == 2)
					{
						return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter out of range (%.02f not between 0.00 and 99.99)", index + 1, displayZoneParameter.zoneVariable1 * 0.01f);
					}
				}
			}
			else if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeClcok)
			{
				int hour = displayZoneParameter.zoneVariable1 / 0x100;
				int minute = displayZoneParameter.zoneVariable1 % 0x100;
				if ((hour < 0 || hour > 99) || (minute < 0 || minute > 99))
				{
					return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter out of range (%d not between 0 and 99)", index + 1, displayZoneParameter.zoneVariable1);
				}
			}
			else if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeString)
			{
				if (displayZoneParameter.zoneVariable1 < 1 || displayZoneParameter.zoneVariable1 > 14 || displayZoneParameter.zoneVariable1 == 3)
				{

					return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter out of range (%d)", index + 1, displayZoneParameter.zoneVariable1);
				}
			}
		}
		else
		{
			if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeArithmetic)
			{
				if (displayZoneParameter.zoneVariable1 < 0 || displayZoneParameter.zoneVariable1 > 999)
				{
					if (displayZoneParameter.zoneVariable2 == 0)
					{

						return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter out of range (%d not between 0 and 999)", index + 1, displayZoneParameter.zoneVariable1);
					}
					else if (displayZoneParameter.zoneVariable2 == 1)
					{
						return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter out of range (%.01f not between 0.0 and 99.9)", index + 1, displayZoneParameter.zoneVariable1 * 0.1f);
					}
					else if (displayZoneParameter.zoneVariable2 == 2)
					{
						return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter out of range (%.02f not between 0.00 and 9.99)", index + 1, displayZoneParameter.zoneVariable1 * 0.01f);
					}
				}
			}
			else if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeClcok)
			{
				return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter doesn't allow Clcok Type", index + 1);
			}
			else if (displayZoneParameter.zoneType == DCDisplayZoneParameter.DCDisplayZoneTypeString)
			{
				if (displayZoneParameter.zoneVariable1 < 1 || displayZoneParameter.zoneVariable1 > 12 || displayZoneParameter.zoneVariable1 == 3 || displayZoneParameter.zoneVariable1 == 9 || displayZoneParameter.zoneVariable1 == 10)
				{

					return new DCError(DCEquipment.DCErrorCodeWrongParameter, "displayZone%dParameter out of range (%d)", index + 1, displayZoneParameter.zoneVariable1);
				}
			}
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
		return 27;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xDB)
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
