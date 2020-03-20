package com.appdevice.domyos.commands.treadmill;

import com.appdevice.domyos.commands.DCGetSerialNumberCommand;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DCTreadmillGetSerialNumberCommand extends DCGetSerialNumberCommand
{
	@Override
	protected HashMap<String, Object> processResponse(byte[] expectedResponse)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, expectedResponse.length - 3);
		
		int consoleYear = byteBuffer.get() & 0xff;
		int consoleWeek = byteBuffer.get() & 0xff;
		int consoleSubSerial = ((byteBuffer.get() & 0xff) << 16 | (byteBuffer.get() & 0xff) << 8 | (byteBuffer.get() & 0xff));
		String consoleFirmwareSerialNumber = String.format("%02d%02d%05d",consoleYear,consoleWeek,consoleSubSerial);

		int mcbYear = byteBuffer.get() & 0xff;
		int mcbWeek = byteBuffer.get() & 0xff;
		int mcbSubSerial = ((byteBuffer.get() & 0xff) << 16 | (byteBuffer.get() & 0xff) << 8 | (byteBuffer.get() & 0xff));
		String mcbFirmwareSerialNumber = String.format("%02d%02d%05d",mcbYear,mcbWeek,mcbSubSerial);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("consoleFirmwareSerialNumber", consoleFirmwareSerialNumber);
		map.put("mcbFirmwareSerialNumber", mcbFirmwareSerialNumber);
		
		return map;
	}
}
