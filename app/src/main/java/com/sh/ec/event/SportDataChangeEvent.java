package com.sh.ec.event;

import com.sh.ec.bluetooth.common.BluetoothSportStats;

public class SportDataChangeEvent {

    public int action;

    public BluetoothSportStats bluetoothSportStats;

    public SportDataChangeEvent(int action, BluetoothSportStats bluetoothSportStats) {
        this.action = action;
        this.bluetoothSportStats = bluetoothSportStats;
    }
    public static final int ACTION_RPM = 120;
    public static final int ACTION_DIS = 121;
    public static final int ACTION_CALORIE = 122;
    public static final int ACTION_SPEED= 123;
    public static final int ACTION_AVG_SPEED= 124;
    public static final int ACTION_PACE = 125;
    public static final int ACTION_HEART_RATE = 126;
    public static final int ACTION_RESISTANCE = 127;
    public static final int ACTION_INCLINE = 128;
    public static final int ACTION_TIME_PER_500 = 129;
    public static final int ACTION_WATT= 130;

}
