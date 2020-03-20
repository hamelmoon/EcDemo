package com.appdevice.domyos.commands.treadmill;

import com.appdevice.domyos.commands.DCGetUsageHourCommand;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DCTreadmillGetUsageHourCommand extends DCGetUsageHourCommand
{
	@Override
	protected HashMap<String, Object> processResponse(byte[] expectedResponse)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, expectedResponse.length - 3);
		
		int consoleUsageHour = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF));
		int mcbUsageHour = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF));

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("consoleUsageHour", Integer.valueOf(consoleUsageHour));
		map.put("mcbUsageHour", Integer.valueOf(mcbUsageHour));
		
		return map;
	}
}
