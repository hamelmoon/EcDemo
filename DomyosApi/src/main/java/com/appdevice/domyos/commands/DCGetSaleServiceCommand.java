package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCGetSaleServiceCommand extends DCCommand
{

	@Override
	protected int getCompatibilityModes()
	{
		return DCEquipment.DCEquipmentModeSetting;
	}

	@Override
	protected byte[] getRequestData()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
		byteBuffer.put((byte) 0xF0);
		byteBuffer.put((byte) 0xC6);

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
		if (uncheckedResponse[1] == (byte) 0xD6)
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
		int saleService = byteBuffer.get() & 0xFF;

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("saleService", Integer.valueOf(saleService));

		return map;
	}

	@Override
	protected DCError getParameterError()
	{
		return null;
	}

}
