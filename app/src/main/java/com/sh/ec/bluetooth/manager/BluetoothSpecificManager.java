package com.sh.ec.bluetooth.manager;

/**
 * Interface that sub managers need to implements to send information to the manager
 *
 * Created by mbouchagour on 12/05/2017.
 */
public interface BluetoothSpecificManager {
  /**
   * Handle communication with manager
   */
  void notifyManager();
}
