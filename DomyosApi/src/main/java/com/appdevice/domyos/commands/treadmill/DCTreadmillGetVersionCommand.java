package com.appdevice.domyos.commands.treadmill;

import com.appdevice.domyos.commands.DCGetVersionCommand;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DCTreadmillGetVersionCommand extends DCGetVersionCommand
{
	
	
	@Override
	protected HashMap<String, Object> processResponse(byte[] expectedResponse)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, expectedResponse.length - 3);
		
		int consoleMajorFirmwareVersion = byteBuffer.get() & 0xFF;
		int consoleMinorFirmwareVersion = byteBuffer.get() & 0xFF;
		float consoleFirmwareVersion = consoleMajorFirmwareVersion + (consoleMinorFirmwareVersion / 10.0f);
		
		int mcbMajorFirmwareVersion = byteBuffer.get() & 0xFF;
		int mcbMinorFirmwareVersion = byteBuffer.get() & 0xFF;
		float mcbFirmwareVersion = mcbMajorFirmwareVersion + (mcbMinorFirmwareVersion / 10.0f);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("consoleFirmwareVersion", Float.valueOf(consoleFirmwareVersion));
		map.put("mcbFirmwareVersion", Float.valueOf(mcbFirmwareVersion));
		
		return map;
	}
}
