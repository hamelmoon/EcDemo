package com.sh.ec.bluetooth.manager.computations;

import android.content.res.TypedArray;

import com.sh.ec.AppContext;
import com.sh.ec.R;
import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.bluetooth.manager.BluetoothEquipmentSpecificManager;

/**
 * Computation strategy to use for Console of type 3 (for devices 8368167, 8369244) and 4 (v < 1.1)
 * according to specs
 */
public class ComputeKCALStrategyConsole3v2 implements ComputeKCALStrategy {
  private final TypedArray torqueEquivalents = AppContext.getInstance()
      .getResources()
      .obtainTypedArray(R.array.torqueEquivalentsConsole3v2);

  public static final int[] affectedIdList =
          AppContext.getInstance().getResources().getIntArray(R.array.computedCalorie3v2IdList);

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

    //compute watt*5.16f (for error diminution) value according to given spec
    float wattDot516 =
        (float) ((valTorque * 2.0f * Math.PI * bluetoothSportStats.getRpm() * 5.16f) / 60.0f);

    //compute kCal per hour according to defined spec
    return (123.45f - (0.12f * bluetoothSportStats.getRpm())) + wattDot516;
  }
}
