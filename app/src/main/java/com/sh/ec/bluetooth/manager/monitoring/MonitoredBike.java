package com.sh.ec.bluetooth.manager.monitoring;

/**
 * Interface used by the monitor to trigger his monitored object actions
 *
 * Created by mbouchagour on 20/03/2018.
 */
public interface MonitoredBike {
  void notifyRpmPause();
  void notifyRpmStart();
  float getMonitoredBikeRPM();
}
