package com.sh.ec.bluetooth.manager;

import com.appdevice.domyos.DCEquipment;
import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.entity.EquipmentInfo;
import com.sh.ec.entity.PauseCauseEnum;


/**
 * Interface implemented by equipment sub managers, used to know generic information about the
 * connected equipment.
 *
 * Created by mbouchagour on 16/05/2017.
 */
public interface BluetoothEquipmentSpecificManager extends BluetoothSpecificManager {

  /**
   * Get the current sports data retrieved
   */
  BluetoothSportStats getBluetoothSportStats();

  /**
   * Get the weight of the user or default value if no user can be used
   */
  float getWeight();

  /**
   * Get the current equipment
   */
  DCEquipment getEquipment();

  /**
   * Request a pause on the equipment
   */
  void pauseClicked(PauseCauseEnum pauseCauseEnum);

  /**
   * Check if the equipment is in pause done
   */
  boolean isStarted();

  /**
   * Check if a session is running
   */
  boolean isProgramStarted();

  /**
   * Check if the equipment is on tab done
   */
  boolean isOnTab();

  /**
   * Clear session data
   */
  void clearSessionData();

  /**
   * request resistance change
   */
  void setResistance(float value);


  /**
   * Start the equipment session
   */
  void startProgram();

  /**
   * Stop the equipment session
   */
  void stopProgram();

  /**
   * request speed change
   */
  void setSpeedCmd(float value);

  /**
   * get information about the equipment
   * @return
   */
  EquipmentInfo getEquipmentInfo();

  /**
   * Clear listeners
   */
  void clearSportDataListeners();

}
