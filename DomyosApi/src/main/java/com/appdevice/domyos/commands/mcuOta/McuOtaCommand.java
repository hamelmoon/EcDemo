package com.appdevice.domyos.commands.mcuOta;

import com.appdevice.domyos.DCCommand;
import com.appdevice.domyos.DCError;

import java.util.HashMap;

/**
 * author : SimonWang
 * date : 2019-12-25 09:08
 * description :
 */
public class McuOtaCommand extends DCCommand {
    @Override
    protected int getCompatibilityModes() {
        return 0;
    }

    @Override
    protected byte[] getRequestData() {
        return new byte[0];
    }

    @Override
    protected DCError getParameterError() {
        return null;
    }

    @Override
    protected byte getExpectedResponseFirstValue() {
        return 0;
    }

    @Override
    protected int getExpectedResponseLength() {
        return 0;
    }

    @Override
    protected boolean isExpectedResponse(byte[] uncheckedResponse) {
        return false;
    }

    @Override
    protected HashMap<String, Object> processResponse(byte[] expectedResponse) {
        return null;
    }
}
