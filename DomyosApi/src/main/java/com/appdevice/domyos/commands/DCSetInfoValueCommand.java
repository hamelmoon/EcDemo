package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.parameters.DCSetInfoParameters;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCSetInfoValueCommand extends DCCommand
{
	public DCSetInfoParameters mSetInfoParameters;

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(23);
		byteBuffer.put((byte) 0xF0);
		byteBuffer.put((byte) 0xAD);

		byteBuffer.put((byte) 0xFF);
		byteBuffer.put((byte) 0xFF);

		byteBuffer.put((byte) (mSetInfoParameters.mCurrentSpeedKmPerHour / 0x100));
		byteBuffer.put((byte) (mSetInfoParameters.mCurrentSpeedKmPerHour % 0x100));

		byteBuffer.put((byte) 0xFF);
		byteBuffer.put((byte) 0xFF);

		byteBuffer.put((byte) 0xFF);
		byteBuffer.put((byte) 0xFF);

		byteBuffer.put((byte) mSetInfoParameters.mTorqueResistanceLevel);

		byteBuffer.put((byte) 0xFF);
		byteBuffer.put((byte) 0xFF);

		byteBuffer.put((byte) (mSetInfoParameters.mTargetInclinePercentage / 0x100));
		byteBuffer.put((byte) (mSetInfoParameters.mTargetInclinePercentage % 0x100));

		byteBuffer.put((byte) (mSetInfoParameters.mWatt / 0x100));
		byteBuffer.put((byte) (mSetInfoParameters.mWatt % 0x100));

		byteBuffer.put((byte) mSetInfoParameters.mHeartRateLedColor);

		byteBuffer.put((byte) mSetInfoParameters.mBtLedSwitch);

		byteBuffer.put((byte) 0xFF);
		byteBuffer.put((byte) 0xFF);

		byteBuffer.put((byte) 0xFF);

		byte checksum = generateChecksum(byteBuffer.array());
		byteBuffer.put(checksum);

		return byteBuffer.array();
	}

	@Override
	protected DCError getParameterError()
	{
		if (mSetInfoParameters == null)
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "Please set the parameters");
			return error;
		}

		if (mSetInfoParameters.mCurrentSpeedKmPerHour != 0xFFFF && (mSetInfoParameters.mCurrentSpeedKmPerHour < 0 || mSetInfoParameters.mCurrentSpeedKmPerHour > 254))
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "currentSpeedKmPerHour out of range (%d not between 0.0 and 25.4)", mSetInfoParameters.mCurrentSpeedKmPerHour);
			return error;
		}
		else if (mSetInfoParameters.mTorqueResistanceLevel != 0xFF && (mSetInfoParameters.mTorqueResistanceLevel < 1 || mSetInfoParameters.mTorqueResistanceLevel > 32))
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "torqueResistanceLevel out of range (%d not between 1 and 32)", mSetInfoParameters.mTorqueResistanceLevel);
			return error;
		}
		else if (mSetInfoParameters.mTargetInclinePercentage != 0xFFFF && (mSetInfoParameters.mTargetInclinePercentage < 0 || mSetInfoParameters.mTargetInclinePercentage > 1200))
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "inclinePercentage out of range (%d not between 0.0 and 20.0)", mSetInfoParameters.mTargetInclinePercentage);
			return error;
		}
		else if (mSetInfoParameters.mWatt != 0xFFFF && (mSetInfoParameters.mWatt < 0 || mSetInfoParameters.mWatt > 1000))
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "watt out of range (%d not between 0 and 1000)", mSetInfoParameters.mWatt);
			return error;
		}
		else if (mSetInfoParameters.mHeartRateLedColor != 0xFF && (mSetInfoParameters.mHeartRateLedColor < 0 || mSetInfoParameters.mHeartRateLedColor > 7))
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "heartRateLedColor out of range (%d not between 0 and 7)", mSetInfoParameters.mHeartRateLedColor);
			return error;
		}
		else if (mSetInfoParameters.mBtLedSwitch != 0xFF && (mSetInfoParameters.mBtLedSwitch < 0 || mSetInfoParameters.mBtLedSwitch > 1))
		{
			DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "btLedSwitch out of range (%d not between 0 and 1)", mSetInfoParameters.mBtLedSwitch);
			return error;
		}
		else
		{
			return null;
		}
	}

	@Override
	protected byte getExpectedResponseFirstValue()
	{
		return (byte) 0xF0;
	}

	@Override
	protected int getExpectedResponseLength()
	{
		return 23;
	}

	@Override
	protected boolean isExpectedResponse(byte[] uncheckedResponse)
	{
		if (uncheckedResponse[1] == (byte) 0xBD)
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
