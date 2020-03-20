package com.sh.ec.bluetooth.manager.computations;


import com.sh.ec.bluetooth.manager.BluetoothEquipmentSpecificManager;

/**
 * Used to uniform calorie computations
 *
 * Created by mbouchagour on 16/05/2017.
 */
public interface ComputeKCALStrategy {
  /**
   * Returns the kCal per hour
   */
  float computeKCALValue(BluetoothEquipmentSpecificManager bluetoothSpecificManager);
}
