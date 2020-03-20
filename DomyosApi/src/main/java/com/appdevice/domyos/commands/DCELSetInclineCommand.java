package com.appdevice.domyos.commands;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.parameters.DCSetInfoParameters;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * author : SimonWang
 * date : 2019-12-24 11:27
 * description :
 */
public class DCELSetInclineCommand extends DCCommand {

    public DCSetInfoParameters mSetInfoParameters;

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
        byteBuffer.put((byte) 0xE3);
        byteBuffer.put((byte) mSetInfoParameters.mIncline);
        byte checksum = generateChecksum(byteBuffer.array());
        byteBuffer.put(checksum);

        return byteBuffer.array();
    }

    @Override
    protected DCError getParameterError() {
        if (mSetInfoParameters == null) {
            DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "Please set the parameters");
            return error;
        }
        if (mSetInfoParameters.mIncline != 0xFF && (mSetInfoParameters.mIncline < 0 || mSetInfoParameters.mIncline > 15)) {
            DCError error = new DCError(DCEquipment.DCErrorCodeWrongParameter, "inclineLevel out of range (%d not between 0 and 20)", mSetInfoParameters.mIncline);
            return error;
        }
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
        if (uncheckedResponse[1] == (byte) 0xF3) {

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
        return null;
    }

}
