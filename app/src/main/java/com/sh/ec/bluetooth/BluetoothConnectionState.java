package com.sh.ec.bluetooth;

/**
 * Display the current state of the connection with the domyos api
 *
 * Created by mbouchagour on 25/04/2017.
 */
public enum BluetoothConnectionState {
  NOT_ENABLED, //
  NOT_INITIALIZED, //
  INITIALIZED, //
  SCANNING, //
  WAITING_CONNECTION_ID,//
  WAITING_SELECTED_EQUIPMENT_DETECTION,//
  CONNECTED, //
  ERROR, TIME_OUT, DISCONNECTED,REJECTED_BY_EQUIPMENT //
}