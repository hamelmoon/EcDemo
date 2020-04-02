package com.sh.ec.bluetooth.manager.monitoring;


import com.sh.ec.bluetooth.manager.utils.DCUnit;

/**
 * Interface providing action with a monitor object
 *
 * Created by mbouchagour on 20/03/2018.
 */
public interface EquipmentDataMonitor {
  void dataReceived(DCUnit unit, float value);
  void resumePause();
  void pauseRequested();
  void startMonitoring();
  void stopMonitoring();
}
