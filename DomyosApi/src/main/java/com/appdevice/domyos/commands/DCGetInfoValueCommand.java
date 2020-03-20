package com.appdevice.domyos.commands;

import android.util.Log;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEllipticalTrainer;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class DCGetInfoValueCommand extends DCCommand {
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
        DCEllipticalTrainer.commandCount++;
        ByteBuffer byteBuffer;
        if (DCEllipticalTrainer.commandCount % 2 == 0) {
            byteBuffer = ByteBuffer.allocate(3);
            byteBuffer.put((byte) 0xF0);
            byteBuffer.put((byte) 0xAC);
            byte checksum = generateChecksum(byteBuffer.array());
            byteBuffer.put(checksum);
        } else {
            byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.put((byte) 0xF0);
            byteBuffer.put((byte) 0xE4);
            byteBuffer.put((byte) 0);
            byte checksum = generateChecksum(byteBuffer.array());
            byteBuffer.put(checksum);

        }
        Log.d("count", "count=" + DCEllipticalTrainer.commandCount);
        return byteBuffer.array();
    }

    @Override
    protected byte getExpectedResponseFirstValue() {
        return (byte) 0xF0;
    }

    @Override
    protected int getExpectedResponseLength() {
        if (DCEllipticalTrainer.commandCount % 2 != 0) return 4;
        return 26;
    }

    @Override
    protected boolean isExpectedResponse(byte[] uncheckedResponse) {
        if (DCEllipticalTrainer.commandCount % 2 == 0) {
            if (uncheckedResponse[1] == (byte) 0xBC) {

                byte[] data = Arrays.copyOf(uncheckedResponse, uncheckedResponse.length - 1);
                byte checksum = generateChecksum(data);
                byte receiveChecksum = uncheckedResponse[uncheckedResponse.length - 1];
                if (checksum == receiveChecksum) {
                    return true;
                }
            }
        } else {
            if (uncheckedResponse[1] == (byte) 0xF4) {

                byte[] data = Arrays.copyOf(uncheckedResponse, uncheckedResponse.length - 1);
                byte checksum = generateChecksum(data);
                byte receiveChecksum = uncheckedResponse[uncheckedResponse.length - 1];
                if (checksum == receiveChecksum) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected HashMap<String, Object> processResponse(byte[] expectedResponse) {
        HashMap<String, Object> map;
        if (DCEllipticalTrainer.commandCount % 2 == 0) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(expectedResponse, 2, expectedResponse.length - 3);

            byteBuffer.position(2);

            int count = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

            byteBuffer.position(4);

            int watt = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

            float currentSpeedKmPerHour = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

            int currentRPM = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

            int currentSessionCumulativeKCal = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

            byteBuffer.position(12);

            float currentSessionCumulativeKM = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

            int torqueResistanceLevel = byteBuffer.get() & 0xFF;

            int errorNumber = byteBuffer.get() & 0xFF;

            int tapOnEquipment = byteBuffer.get() & 0xFF;

            int analogHeartRate = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF));

            float currentSessionAverageSpeed = ((byteBuffer.get() & 0xFF) * 0x100 + (byteBuffer.get() & 0xFF)) / 10.0f;

            byteBuffer.position(22);

            int pressedButton = byteBuffer.get() & 0xff;

            int fanSpeedLevel = byteBuffer.get() & 0xff;

            int hotKeyStatus = byteBuffer.get() & 0xff;

            map = new HashMap<String, Object>();
            map.put("watt", Integer.valueOf(watt));
            map.put("currentSpeedKmPerHour", Float.valueOf(currentSpeedKmPerHour));
            map.put("currentRPM", Integer.valueOf(currentRPM));
            map.put("count", Integer.valueOf(count));
            map.put("currentSessionCumulativeKCal", Integer.valueOf(currentSessionCumulativeKCal));
            map.put("currentSessionCumulativeKM", Float.valueOf(currentSessionCumulativeKM));
            map.put("torqueResistanceLevel", Integer.valueOf(torqueResistanceLevel));
            map.put("errorNumber", Integer.valueOf(errorNumber));
            map.put("tapOnEquipment", Integer.valueOf(tapOnEquipment));
            map.put("analogHeartRate", Integer.valueOf(analogHeartRate));
            map.put("currentSessionAverageSpeed", Float.valueOf(currentSessionAverageSpeed));
            map.put("pressedButton", Integer.valueOf(pressedButton));
            map.put("fanSpeedLevel", Integer.valueOf(fanSpeedLevel));
            map.put("hotKeyStatus", Integer.valueOf(hotKeyStatus));
        } else {
            int incline = expectedResponse[2] & 0XFF;
            map = new HashMap<String, Object>();
            map.put("incline", Integer.valueOf(incline));
            Log.e("simon", "incline=" + Integer.valueOf(incline));
        }

        return map;

    }

    @Override
    protected DCError getParameterError() {
        return null;
    }

}
