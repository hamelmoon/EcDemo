package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCGetErrorHistoricListCommand extends DCCommand
{

    @Override
    protected int getCompatibilityModes()
    {
        return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
    }

    @Override
    protected byte[] getRequestData()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.put((byte) 0xF0);
        byteBuffer.put((byte) 0xC7);

        byte checksum = generateChecksum(byteBuffer.array());
        byteBuffer.put(checksum);

        return byteBuffer.array();
    }

    @Override
    protected DCError getParameterError()
    {
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
        return 13;
    }

    @Override
    protected boolean isExpectedResponse(byte[] uncheckedResponse)
    {
        if (uncheckedResponse[1] == (byte) 0xD7)
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
        int length = expectedResponse.length - 3;
        ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, length);
        int[] errorHistoricList = new int[length];
        int errorCount = 0;

        for (int i = 0; i < length; i++)
        {
            int error = byteBuffer.get() & 0xff;
            errorHistoricList[i] = error;
            if (error > 0)
            {
                errorCount++;
            }
        }

        int[] finalErrorHistoricList = new int[errorCount];

        System.arraycopy(errorHistoricList, 0, finalErrorHistoricList, 0, errorCount);

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("errorHistoricList", finalErrorHistoricList);
        return map;
    }

}
