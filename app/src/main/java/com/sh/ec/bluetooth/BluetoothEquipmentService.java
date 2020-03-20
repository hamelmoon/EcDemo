package com.sh.ec.bluetooth;

import android.content.Context;

import com.sh.ec.bluetooth.common.BluetoothDiscoveredEquipment;
import com.sh.ec.bluetooth.common.BluetoothEquipmentConnectionState;
import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.bluetooth.common.EquipmentPauseState;
import com.sh.ec.entity.EquipmentInfo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;


/**
 * Interface that provide methods to communicate with BluetoothManager
 *
 * Created by mbouchagour on 21/04/2017.
 */
public interface BluetoothEquipmentService {

  /**
   * Returning the {@link BluetoothSportStats}
   *
   * @return the bluetooth sport stats
   */
  Observable<BluetoothSportStats> getBluetoothSportStats();

  /**
   * listen pause done changes method
   *
   * @param pause the pause
   * @return the observable
   */
  Observable<EquipmentPauseState> listenPauseState(boolean pause);

  /**
   * listen to tab done changes method
   *
   * @return the observable
   */
  Observable<EquipmentPauseState> listenTabState();

  /**
   * request pause session method
   *
   * @return the observable
   */
  Observable<EquipmentPauseState> pause();

  /**
   * request start session method
   *
   * @return the observable
   */
  Observable<EquipmentPauseState> start();

  /**
   * request resistance change
   *
   * @param value the value
   * @return true if successful
   */
  Observable<Boolean> setResistance(float value);

  /**
   * request speed change
   *
   * @param value the value
   * @return true if successful
   */
  Observable<Boolean> setSpeedCmd(float value);



  /**
   * request information about connected equipment
   *
   * @return the equipment info
   */
  Observable<EquipmentInfo> getEquipmentInfo();

  /**
   * Returning the id of a new equipment discovered
   *
   * @return the observable
   */
  Observable<List<BluetoothDiscoveredEquipment>> listenScan();

  /**
   * Returning the list of a new equipment discovered
   *
   * @return the equipment list
   */
  Observable<List<BluetoothDiscoveredEquipment>> getEquipmentList();

  Observable<String> findEquipmentById(int sportID);

  /**
   * Used to refresh equipment list (implicitly refresh equipment names on repository)
   */
  void updateEquipmentList();

  Observable<String> getEquipmentId();

  /**
   * Set the bluetooth manager connection done
   *
   * @param state the done
   * @return the done
   */
  Single<Void> setState(BluetoothConnectionState state);

  /**
   * Connect the specified equipment
   *
   * @param equipmentName the equipment name
   * @return the single
   */
  Single<Void> connectEquipment(String equipmentName);

  /**
   * Connect the specified equipment
   *
   * @param equipmentName the equipment name
   */
  void setSelectedEquipmentName(String equipmentName);

  /**
   * Gets selected id console.
   *
   * @return the selected id console
   */
  int getSelectedIdConsole();

  /**
   * Disconnect the specified element
   *
   * @return the single
   */
  Observable<Void> disconnectEquipment();
/**
   * Clear the specified element data
   *
   * @return the single
   */
  Observable<Void> clearSessionData();


  /**
   * Start the equipment session
   * @return
   */
  Observable<Void> startProgram();

  /**
   * Start the equipment session
   * @return
   */
  Observable<Void> stopProgram();

  /**
   * Register to listen connection changes
   *
   * @return the observable
   */
  Observable<BluetoothEquipmentConnectionState> listenConnectionStates(Context context);
}
