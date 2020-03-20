package com.appdevice.domyos.commands.treadmill;

import com.appdevice.domyos.commands.DCGetCumulativeKMCommand;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class DCTreadmillGetCumulativeKMCommand extends DCGetCumulativeKMCommand
{
	@Override
	protected HashMap<String, Object> processResponse(byte[] expectedResponse)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, expectedResponse.length - 3);
		
		int cumulativeKM = ((byteBuffer.get() & 0xFF)* 0x100 + (byteBuffer.get() & 0xFF));
		
		int mcbCumulativeKM = ((byteBuffer.get() & 0xFF) << 16 | (byteBuffer.get() & 0xFF) << 8 | (byteBuffer.get() & 0xFF));

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("consoleCumulativeKM", Integer.valueOf(cumulativeKM));
		map.put("mcbCumulativeKM", Integer.valueOf(mcbCumulativeKM));
		
		return map;
	}
}
