package com.sh.ec.bluetooth.manager.computations;

import android.content.res.TypedArray;

import com.sh.ec.AppContext;
import com.sh.ec.R;
import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.bluetooth.manager.BluetoothEquipmentSpecificManager;

//FIXME Magic number

/**
 * Computation strategy to use for Console of type 3 (v < 1.6) for equipment 8363074, 8369237,
 * 8368168,
 * 8369246 according to specs
 */
public class ComputeKCALStrategyConsole3v1 implements ComputeKCALStrategy {
  private final TypedArray torqueEquivalents = AppContext.getInstance()
      .getResources()
      .obtainTypedArray(R.array.torqueEquivalentsConsole3v1);
  public static final int[] affectedIdList =
          AppContext.getInstance().getResources().getIntArray(R.array.computedCalorie3v1IdList);

  @Override
  public float computeKCALValue(BluetoothEquipmentSpecificManager bluetoothSpecificManager) {
    BluetoothSportStats bluetoothSportStats = bluetoothSpecificManager.getBluetoothSportStats();

    //if rpm are nul or not yet obtained no need to do all computations
    if (bluetoothSportStats.getRpm() == 0f) {
      return 0f;
    }

    float valTorque = torqueEquivalents.getFloat(
        (int)bluetoothSportStats.getResistance() >= torqueEquivalents.length() ? torqueEquivalents
            .length() - 1 : (int)bluetoothSportStats.getResistance(), 0);

    //compute watt value according to given spec
    float watt = (float) ((valTorque * 2.0f * Math.PI * bluetoothSportStats.getRpm()) / 60.0f);

    //compute kCal per hour according to defined spec
    return ((((watt * 11.33f) + 452.0f) * 4.862f * 60.0f) / 1000.0f);
  }
}
