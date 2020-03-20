package com.sh.ec.bluetooth.common;

import com.appdevice.domyos.DCEquipment;
import com.sh.ec.bluetooth.BluetoothConnectionState;
import com.sh.ec.utils.DomyosException;


/**
 * Class used to pass connection states with domyos equipment
 *
 * Created by mbouchagour on 25/04/2017.
 */
public class BluetoothEquipmentConnectionState {

  private int equipmentNumber;
  private BluetoothConnectionState connectionState;
  private DCEquipment connectedEquipment;
  private DomyosException domyosException;

  public DomyosException getDomyosException() {
    return domyosException;
  }

  public void setDomyosException(DomyosException domyosException) {
    this.domyosException = domyosException;
  }

  public int getEquipmentNumber() {
    return equipmentNumber;
  }

  public void setEquipmentNumber(int equipmentNumber) {
    this.equipmentNumber = equipmentNumber;
  }

  public BluetoothConnectionState getConnectionState() {
    return connectionState;
  }

  public void setConnectionState(BluetoothConnectionState connectionState) {
    this.connectionState = connectionState;
  }

  public DCEquipment getConnectedEquipment() {
    return connectedEquipment;
  }

  public void setConnectedEquipment(DCEquipment connectedEquipment) {
    this.connectedEquipment = connectedEquipment;
  }
}
