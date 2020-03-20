package com.appdevice.domyos.commands;

import android.util.Log;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * author : SimonWang
 * date : 2019-12-24 10:38
 * description :
 */
public class DCELGetInclineCommand extends DCCommand {
    @Override
    protected int getCompatibilityModes() {
        return DCEquipment.DCEquipmentModeSetting | DCEquipment.DCEquipmentModeWorkout;
    }

    @Override
    protected int getRetryCount() {
        return 5;
    }

    @Override
    protected float getRetryTimeout() {
        return 1.9f;
    }

    @Override
    protected byte[] getRequestData() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.put((byte) 0xF0);
        byteBuffer.put((byte) 0xE4);
        byteBuffer.put((byte) 0);
        byte checksum = generateChecksum(byteBuffer.array());
        byteBuffer.put(checksum);

        return byteBuffer.array();
    }

    @Override
    protected DCError getParameterError() {
        return null;
    }

    @Override
    protected byte getExpectedResponseFirstValue() {
        return (byte) 0xF0;
    }

    @Override
    protected int getExpectedResponseLength() {
        return 4;
    }

    @Override
    protected boolean isExpectedResponse(byte[] uncheckedResponse) {
        if (uncheckedResponse[1] == (byte) 0xF4) {

            byte[] data = Arrays.copyOf(uncheckedResponse, uncheckedResponse.length - 1);
            byte checksum = generateChecksum(data);
            byte receiveChecksum = uncheckedResponse[uncheckedResponse.length - 1];
            if (checksum == receiveChecksum) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected HashMap<String, Object> processResponse(byte[] expectedResponse) {
        int incline = expectedResponse[2] & 0XFF;
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("incline", Integer.valueOf(incline));
        Log.e("simon", "incline=" + Integer.valueOf(incline));
        return map;
    }

}
