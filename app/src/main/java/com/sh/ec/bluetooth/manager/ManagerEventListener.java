package com.sh.ec.bluetooth.manager;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.sh.ec.bluetooth.common.BluetoothEquipmentConnectionState;
import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.bluetooth.common.EquipmentPauseState;
import com.sh.ec.entity.EquipmentInfo;

import java.util.Collection;

/**
 * Define events that will be received by the bluetooth manager
 *
 * Created by mbouchagour on 12/05/2017.
 */
public interface ManagerEventListener {

  /**
   * Occurs when a sport data is received
   */
  void onSportDataReceived(BluetoothSportStats bluetoothSportStats);

  /**
   * Occurs when an equipment error occurs
   */
  void onError(DCEquipment equipment, DCError error);

  /**
   * Occurs when the equipment info is retreived
   */
  void onEquipmentInfoReceived(EquipmentInfo equipmentInfo);

  /**
   * Occurs when the connection done has changed
   */
  void onConnectionStateReceived(
          BluetoothEquipmentConnectionState bluetoothEquipmentConnectionState);

  /**
   * Notify that a new equipment as been discovered
   */
  void onEquipmentDiscovered(Collection<DCEquipment> equipmentsDiscovered);

  /**
   * Notify when an equipment has been connected
   */
  void onEquipmentConnected(DCEquipment connectedEquipment);

  /**
   * Notify when the search for the selected equipment is processing to know if the equipment is still
   *
   * turned on or in range
   */
  void onEquipmentSearch(DCEquipment selectedEquipment);

  /**
   * Occurs when an equipment has been disconnected
   */
  void onEquipmentDisconnected();

  /**
   * Occurs when the Id of the equipment is received
   */
  void onEquipmentIdReceived(String id);

  /**
   * Occurs when an equipment pause is requested
   */
  void onEquipmentPause(EquipmentPauseState equipmentPauseState);

  /**
   * Occurs when a tab change event is obtained
   */
  void onEquipmentTabChange(boolean onTab);
}
