package com.sh.ec.bluetooth.manager.elliptical;

import com.appdevice.domyos.DCEllipticalTrainer;
import com.appdevice.domyos.DCEllipticalTrainerSportData;
import com.appdevice.domyos.DCEquipment;

/**
 * Interface representing equipment sport callbacks
 *
 * Created by mbouchagour on 19/04/2017.
 */
public interface EllipticalListener extends DCEllipticalTrainer.DCEllipticalTrainerListener,
    DCEllipticalTrainerSportData.DCEllipticalTrainerSportDataListener {
  //console section
  @Override void equipmentTabOnEquipmentChanged(DCEquipment dcEquipment, boolean b);

  @Override void equipmentErrorOccurred(DCEquipment dcEquipment, int i);

  @Override void equipmentPressedButtonChanged(DCEquipment dcEquipment, int i);

  @Override void equipmentOnHotKeyStatusChanged(DCEquipment dcEquipment, int i);

  @Override void equipmentOnFanSpeedLevelChanged(DCEquipment dcEquipment, int i);

  //sport data section
  @Override void onWattChanged(float v);

  @Override void onCurrentRPMChanged(int i);

  @Override void onTorqueResistanceLevelChanged(int i);

  @Override void onCurrentSpeedKmPerHourChanged(float v);

  @Override void onCurrentSessionCumulativeKMChanged(float v);

  @Override void onAnalogHeartRateChanged(int i);

  @Override void onCurrentSessionAverageSpeedChanged(float v);
}
