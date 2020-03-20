package com.sh.ec.event;

import android.bluetooth.BluetoothDevice;

import com.appdevice.domyos.DCBikeSportData;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCTreadmillSportData;
import com.sh.ec.entity.EquipmentInfo;

import java.util.List;

public class EquipmentEvent {
    public int action;
    public EquipmentInfo equipmentInfo;
    public DCEquipment ewEquipment;
    public DCTreadmillSportData treadmillSportData;
    public DCBikeSportData bikeSportData;
    public List<DCEquipment> equipments;
    
    public EquipmentEvent(int action) {
        this.action = action;
    }


    public EquipmentEvent(int action, List<DCEquipment> equipments) {
        this.action = action;
        this.equipments = equipments;
    }

    public EquipmentEvent(int action, DCEquipment ewEquipment) {
        this.action=action;
        this.ewEquipment = ewEquipment;
    }

    public EquipmentEvent(int action, EquipmentInfo equipmentInfo) {
        this.action = action;
        this.equipmentInfo = equipmentInfo;
    }
    
    public BluetoothDevice bluetoothDevice;
    
    public EquipmentEvent(DCTreadmillSportData treadmillSportData) {
        this.action = ACTION_TREADMILL_SPORT;
        this.treadmillSportData = treadmillSportData;
    }
    
    public EquipmentEvent(DCBikeSportData bikeSportData) {
        this.action = ACTION_BIKE_SPORT;
        this.bikeSportData = bikeSportData;
    }

    public static final int ACTION_EQUIPMENT_CONNECTED = 101;
    public static final int ACTION_EQUIPMENT_DISCONNECT = 102;
    public static final int ACTION_EQUIPMENT_SEARCH = 103;
    public static final int ACTION_TREADMILL_SPORT = 104;
    public static final int ACTION_BIKE_SPORT = 105;
    public static final int ACTION_QUICK_START = 106;
    public static final int ACTION_PROGRAM_START = 107;
    public static final int ACTION_STOP = 108;
    public static final int ACTION_PAUSE = 109;
    public static final int ACTION_RESATART = 110;


    
}
