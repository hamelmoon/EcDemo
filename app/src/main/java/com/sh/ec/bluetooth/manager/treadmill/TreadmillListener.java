package com.sh.ec.bluetooth.manager.treadmill;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCTreadmill;
import com.appdevice.domyos.DCTreadmillSportData;

/**
 * Interface representing treadmill sport and console callbacks
 *
 * Created by mbouchagour on 19/04/2017.
 */
public interface TreadmillListener
    extends DCTreadmill.DCTreadmillListener, DCTreadmillSportData.DCTreadmillSportDataListener {

  //console section
  @Override void treadmillOnSafetyMotorKeyChanged(DCEquipment dcEquipment, boolean b);

  @Override void equipmentTabOnEquipmentChanged(DCEquipment dcEquipment, boolean b);

  @Override void equipmentErrorOccurred(DCEquipment dcEquipment, int i);

  @Override void equipmentPressedButtonChanged(DCEquipment dcEquipment, int i);

  @Override void equipmentOnHotKeyStatusChanged(DCEquipment dcEquipment, int i);

  @Override void equipmentOnFanSpeedLevelChanged(DCEquipment dcEquipment, int i);

  //sport data section
  @Override void onTargetInclinePercentageChanged(float v);

  @Override void onCurrentSpeedKmPerHourChanged(float v);

  @Override void onCurrentSessionCumulativeKMChanged(float v);

  @Override void onAnalogHeartRateChanged(int i);

  @Override void onCurrentSessionAverageSpeedChanged(float v);
}
