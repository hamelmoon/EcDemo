package com.sh.ec.event;

public class DeviceEvent {
    public int action;

    public int mode;

    public DeviceEvent(int action, int mode) {
        this.action = action;
        this.mode = mode;
    }

    public DeviceEvent(int action) {
        this.action = action;
    }
    public static final int ACTION_SEARCH = 1;
    public static final int ACTION_RUNNING = 2;
    public static final int ACTION_RESULT = 3;
    public static final int ACTION_COUNT = 4;





}
