package com.sh.ec.bluetooth.manager.bike;

import com.appdevice.domyos.DCBike;
import com.appdevice.domyos.DCBikeSportData;
import com.appdevice.domyos.DCEquipment;

/**
 * Interface representing equipment sport callbacks and interactions with bike console
 *
 * Created by mbouchagour on 19/04/2017.
 */
public interface BikeListener
    extends DCBike.DCBikeListener, DCBikeSportData.DCBikeSportDataListener {
  //console section

  @Override void equipmentTabOnEquipmentChanged(DCEquipment dcEquipment, boolean b);

  /**
   * Equipment error callback
   */
  @Override void equipmentErrorOccurred(DCEquipment dcEquipment, int i);

  /**
   * Called when the user press a button on console
   */
  @Override void equipmentPressedButtonChanged(DCEquipment dcEquipment, int i);

  /**
   * Called when the user press an hotkey on console
   */
  @Override void equipmentOnHotKeyStatusChanged(DCEquipment dcEquipment, int i);

  /**
   * Called when the user press the fan speed level change button
   */
  @Override void equipmentOnFanSpeedLevelChanged(DCEquipment dcEquipment, int i);

  //sport data section

  /**
   * Watt value received from bike
   */
  @Override void onWattChanged(float v);

  /**
   * RPM value received from bike
   */
  @Override void onCurrentRPMChanged(int i);

  /**
   * Torque resistance value received from bike
   */
  @Override void onTorqueResistanceLevelChanged(int i);

  /**
   * Speed value received from bike
   */
  @Override void onCurrentSpeedKmPerHourChanged(float v);

  /**
   * Session distance value received from bike
   */
  @Override void onCurrentSessionCumulativeKMChanged(float v);

  /**
   * Heart rate value received from bike
   */
  @Override void onAnalogHeartRateChanged(int i);

  /**
   * Average speed value received from bike
   */
  @Override void onCurrentSessionAverageSpeedChanged(float v);
}
