package com.sh.ec.bluetooth.manager.monitoring;

import android.os.SystemClock;
import com.sh.ec.bluetooth.manager.utils.DCUnit;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class used to listen to bike data and trigger an action on specific bike manager depending on
 * sport data received
 * Created by mbouchagour on 20/03/2018.
 */
public class BikeMonitor implements EquipmentDataMonitor {
  //todo 16/07/2018 MBR : can be generalized by providing monitored data type to fit every equipment types (rower, elliptical ...)
  private MonitoredBike monitoredBike;
  private static final long DEFAULT_PAUSE_TRIGGER_ELAPSED_TIME = 6000;
  private static final long DEFAULT_START_DATA_TRIGGER_ELAPSED_TIME = 7000;
  private static final long DEFAULT_PAUSE_TRIGGER_ELAPSED_START_TIME = 10000;
  private boolean monitorStarted = false;
  private boolean isFirstLaunch = false;
  private boolean disableRestart = false;
  private long beginZeroTriggerTime = -1;
  private long currentZeroTriggerTime = -1;

  private Timer timer = new Timer();

  public BikeMonitor(MonitoredBike monitoredBike) {
    this.monitoredBike = monitoredBike;
  }

  @Override public void dataReceived(DCUnit unit, float value) {
    if (unit == DCUnit.CURRENT_ROTATION && monitorStarted && value > 0) {
      isFirstLaunch = false;
      //if a value is received it means that the user is currently using the equipment we can reset timers
      initializeTimes();

      if (!disableRestart) {
        //we can start again the session
        monitoredBike.notifyRpmStart();
      }
    }
  }

  /**
   * function that reset timer values to avoid pause trigger during some time
   */
  @Override public void resumePause() {
    initializeTimes();
  }

  /**
   * Notify that the user has requested a pause, the monitor then need to wait some time before any
   * start trigger. This ensure, in that case, that we spend a minimum time in pause to avoid
   * equipment connection latency to trigger immediately a start event.
   */
  @Override public void pauseRequested() {
    if(!disableRestart) {
      disableRestart = true;
      new Timer().schedule(new TimerTask() {
        @Override public void run() {
          disableRestart = false;
        }
      }, DEFAULT_START_DATA_TRIGGER_ELAPSED_TIME);
    }
  }

  /**
   * Check the inactivity of the equipment and trigger a pause if it pass the default elapsed time
   * value
   */
  private void checkRPMValues() {
    currentZeroTriggerTime = SystemClock.elapsedRealtime();
    if ((currentZeroTriggerTime - beginZeroTriggerTime) > getDefaultTriggerTime() && monitoredBike.getMonitoredBikeRPM()==0) {
      monitoredBike.notifyRpmPause();
    }else {
      if(monitoredBike.getMonitoredBikeRPM()!=0){
        dataReceived(DCUnit.CURRENT_ROTATION,monitoredBike.getMonitoredBikeRPM());
      }
    }
  }

  private long getDefaultTriggerTime() {
    if (isFirstLaunch) {
      return DEFAULT_PAUSE_TRIGGER_ELAPSED_START_TIME;
    }
    return DEFAULT_PAUSE_TRIGGER_ELAPSED_TIME;
  }

  /**
   * Used to init begin and current time values
   */
  private void initializeTimes() {
    beginZeroTriggerTime = SystemClock.elapsedRealtime();
    currentZeroTriggerTime = beginZeroTriggerTime;
  }

  @Override public void startMonitoring() {
    monitorStarted = true;
    isFirstLaunch = true;
    initializeTimes();
    timer.schedule(new TimerTask() {
      @Override public void run() {
        checkRPMValues();
      }
    }, 0, 1000);
  }

  @Override public void stopMonitoring() {
    monitorStarted = false;
  }
}
