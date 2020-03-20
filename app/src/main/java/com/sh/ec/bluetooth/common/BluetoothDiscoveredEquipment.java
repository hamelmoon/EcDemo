package com.sh.ec.bluetooth.common;

/**
 * Class used to retrieve the id and the type of the equipment discovered during scan
 *
 * Created by mbouchagour on 25/04/2017.
 */
public class BluetoothDiscoveredEquipment {

  private String equipmentCustomName;
  private String equipmentName;
  private String equipmentID;
  private int equipmentType;

  public String getEquipmentName() {
    return equipmentName;
  }

  public void setEquipmentName(String equipmentName) {
    this.equipmentName = equipmentName;
  }

  public int getEquipmentType() {
    return equipmentType;
  }

  public void setEquipmentType(int equipmentType) {
    this.equipmentType = equipmentType;
  }

  public String getEquipmentID() {
    return equipmentID;
  }

  public void setEquipmentID(String equipmentID) {
    this.equipmentID = equipmentID;
  }

  /**
   *  *@return Retourne la valeur de equipmentCustomName
   *  
   */
  public String getEquipmentCustomName() {
    return equipmentCustomName;
  }

  /**
   *  *Modifie la valeur de equipmentCustomName
   *  
   */
  public void setEquipmentCustomName(String equipmentCustomName) {
    this.equipmentCustomName = equipmentCustomName;
  }
}
