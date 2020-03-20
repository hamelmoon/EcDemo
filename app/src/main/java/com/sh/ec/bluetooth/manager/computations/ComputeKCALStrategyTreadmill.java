package com.sh.ec.bluetooth.manager.computations;


import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.bluetooth.manager.BluetoothEquipmentSpecificManager;

/**
 * Computation strategy to use for Console of type 3
 */
public class ComputeKCALStrategyTreadmill implements ComputeKCALStrategy {

  @Override
  public float computeKCALValue(BluetoothEquipmentSpecificManager bluetoothSpecificManager) {
    BluetoothSportStats bluetoothSportStats = bluetoothSpecificManager.getBluetoothSportStats();
    float speed = bluetoothSportStats.getSpeedKmPerHour();
    //if speed is nul or not yet obtained no need to do all computations
    if (speed == 0) {
      return 0;
    }
    float incline = bluetoothSportStats.getInclinePercentage();
    float weight = bluetoothSpecificManager.getWeight();
    if(weight<=1){
      weight = 75;
    }
    float run = 1;

    if (bluetoothSportStats.getSpeedKmPerHour() >= 7) {
      run = 2;
    }

    //compute kCal per hour according to defined spec
    return (float) ((0.1 * run * speed * 5 * weight) + ((1.8 * speed * incline * 5 * weight) / (100
        * run)));
  }
}
