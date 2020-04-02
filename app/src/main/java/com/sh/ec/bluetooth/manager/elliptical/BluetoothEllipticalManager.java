package com.sh.ec.bluetooth.manager.elliptical;

import android.util.Log;

import com.appdevice.domyos.DCCompletionBlock;
import com.appdevice.domyos.DCCompletionBlockWithError;
import com.appdevice.domyos.DCEllipticalTrainer;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentInfo;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.parameters.et.DCEllipticalTrainerWorkoutModeSetInfoParameters;
import com.sh.ec.AppContext;
import com.sh.ec.R;
import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.bluetooth.common.EquipmentPauseState;
import com.sh.ec.bluetooth.manager.BluetoothEquipmentSpecificManager;
import com.sh.ec.bluetooth.manager.ManagerEventListener;
import com.sh.ec.bluetooth.manager.computations.ComputeKCALStrategy;
import com.sh.ec.bluetooth.manager.computations.ComputeKCALStrategyConsole3v1;
import com.sh.ec.bluetooth.manager.computations.ComputeKCALStrategyConsole3v2;
import com.sh.ec.bluetooth.manager.monitoring.BikeMonitor;
import com.sh.ec.bluetooth.manager.monitoring.EquipmentDataMonitor;
import com.sh.ec.bluetooth.manager.monitoring.MonitoredBike;
import com.sh.ec.bluetooth.manager.utils.BluetoothEquipmentConsoleUtils;
import com.sh.ec.bluetooth.manager.utils.DCUnit;
import com.sh.ec.bluetooth.manager.utils.TypeConstants;
import com.sh.ec.entity.EquipmentInfo;
import com.sh.ec.entity.PauseCauseEnum;
import com.sh.ec.event.SportDataChangeEvent;
import com.sh.ec.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;
import timber.log.Timber;
import static com.sh.ec.bluetooth.manager.BluetoothManagerDemo.EQUIPMENT_FIRMWARE_1_5;
import static com.sh.ec.entity.PauseCauseEnum.EQUIPMENT_ACTIVITY;
import static com.sh.ec.entity.PauseCauseEnum.EQUIPMENT_INACTIVITY;
import static com.sh.ec.entity.PauseCauseEnum.SESSION_START;
import static com.sh.ec.entity.PauseCauseEnum.SESSION_STOP;
import static com.sh.ec.entity.PauseCauseEnum.TAB_DETECTED;
import static com.sh.ec.entity.PauseCauseEnum.TAB_NOT_DETECTED;
import static com.sh.ec.entity.PauseCauseEnum.USER_REQUEST_PAUSE;
import static com.sh.ec.entity.PauseCauseEnum.USER_REQUEST_START;
import static com.sh.ec.service.MangerService.EQUIPMENT_WORKOUT_ERROR_CODE;
import static com.sh.ec.service.MangerService.isConnected;

/**
 * Class used to manage interactions with elliptical bike
 *
 * Created by mbouchagour on 12/05/2017.
 */
public class BluetoothEllipticalManager
  implements EllipticalListener, BluetoothEquipmentSpecificManager, MonitoredBike {
  private static final int BPM_ZONE_INDEX = 1;
  private static final int RPM_ZONE_INDEX = 4;
  private static final int RESISTANCE_ZONE_INDEX = 6;
  private static final int RESISTANCE_MAX = 15;
  private static final int RESISTANCE_MIN = 1;
  private int DEFAULT_EQUIPMENT_SAFE_UNKNOWN_ID_REPLACEMENT = 8368167;
  private static final double CONSOLE_4_NEW_VERSION = 1.2;
  private static final int DEFAULT_WORKOUT_RESEND_DELAY = 2000;

  private static final int[] console3IdList =
    AppContext.getInstance().getResources().getIntArray(R.array.console3IdList);
  private static final int[] console4IdList =
    AppContext.getInstance().getResources().getIntArray(R.array.console4IdList);

  private DCEllipticalTrainer ellipticalTrainer;
  private BluetoothSportStats bluetoothSportStats = new BluetoothSportStats();
  private final EquipmentInfo equipmentInfo = new EquipmentInfo();

  private ManagerEventListener mListener;

  private final DCEllipticalTrainerWorkoutModeSetInfoParameters infoParams =
    new DCEllipticalTrainerWorkoutModeSetInfoParameters();

  private float equipmentVersion = -1f;
  private Integer equipmentID;

  private float weight = 0;
  private ComputeKCALStrategy computationStrategy = null;
  private EquipmentDataMonitor equipmentDataMonitor;

  private int fanSpeed = 0;
  private int torqueResistanceLevel = 1;

  private float lastKnownCumulativeDistance = 0;
  private float lastKnownCumulativeKcal = 0;
  private float lastKcalObtainedFromEquipment = 0f;
  private boolean clearData = false;

  private float currentRPM = 0;

  private Boolean isMetric = true;

  private boolean started = false;

  private boolean simulatedPause = false;

  private final DCCompletionBlockWithError genericErrorBlock = new DCCompletionBlockWithError() {
    @Override public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
            Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
                dcEquipment.getName(), dcError.getDescription());
      if (dcError.getCode() == EQUIPMENT_WORKOUT_ERROR_CODE && dcEquipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
        Timber.i("BLUETOOTH MANAGER DOMYOS WORKOUT ERROR OCCURRED, TRYING DELAYED CALL ...");
        //avoid to spam console by delay calls
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
          @Override public void run() {
            if (isConnected(ellipticalTrainer)) {
              //Switch equipment to workout mode for practice
              if (ellipticalTrainer.getMode() != DCEquipment.DCEquipmentModeWorkout) {
                Timber.i("... BLUETOOTH MANAGER DOMYOS WORKOUT DELAYED CALL");
                ellipticalTrainer.setMode(DCEquipment.DCEquipmentModeWorkout,
                    dcEquipment1 -> onWorkoutCompletion(!isProgramStarted()), genericErrorBlock);
              } else {
                Timber.i("... BLUETOOTH MANAGER DOMYOS WORKOUT DELAYED CALL : ALREADY IN WORKOUT MODE");
                onWorkoutCompletion(!isProgramStarted());
              }
            } else {
              Timber.i("... BLUETOOTH MANAGER DOMYOS WORKOUT DELAYED CALL : NOT CONNECTED, NO MORE CALLS");
            }
          }
        }, DEFAULT_WORKOUT_RESEND_DELAY);
      }
      if (mListener != null) {
        mListener.onError(dcEquipment, dcError);
      }
    }
  };

  private final DCCompletionBlockWithError idErrorBlock = new DCCompletionBlockWithError() {
    @Override public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
      Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
          dcEquipment.getName(), dcError.getDescription());
      if (dcEquipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
        Timber.i("BLUETOOTH MANAGER DOMYOS ID ERROR OCCURRED, TRYING DELAYED CALL ...");
        //avoid to spam console by delay calls
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
          @Override public void run() {
            if (isConnected(ellipticalTrainer)) {
              onWorkoutCompletion(clearData);
            } else {
              Timber.i("... BLUETOOTH MANAGER DOMYOS ID DELAYED CALL : NOT CONNECTED, NO MORE CALLS");
            }
          }
        }, DEFAULT_WORKOUT_RESEND_DELAY);
      }
      if (mListener != null) {
        mListener.onError(dcEquipment, dcError);
      }
    }
  };

  private final DCCompletionBlockWithError infoErrorBlock = new DCCompletionBlockWithError() {
    @Override public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
      Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
          dcEquipment.getName(), dcError.getDescription());
      if (dcEquipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
        Timber.i("BLUETOOTH MANAGER DOMYOS INFO ERROR OCCURRED, TRYING DELAYED CALL ...");
        //avoid to spam console by delay calls
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
          @Override public void run() {
            if (isConnected(ellipticalTrainer)) {
              onIdReceived(String.valueOf(equipmentID),clearData);
            } else {
              Timber.i("... BLUETOOTH MANAGER DOMYOS INFO DELAYED CALL : NOT CONNECTED, NO MORE CALLS");
            }
          }
        }, DEFAULT_WORKOUT_RESEND_DELAY);
      }
      if (mListener != null) {
        mListener.onError(dcEquipment, dcError);
      }
    }
  };

  /**
   * Used to showView in console
   *
   * TODO !!
   */
  private final DCCompletionBlock genericCompletionBlock = dcEquipment -> {
    //empty
  };

  /**
   * Used to trigger equipment pause after session stop and data cleared
   */
  private final DCCompletionBlock sessionStopCompletionBlock = dcEquipment -> {
    Timber.i("BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ------> SUCCESS... ASKING SESSION PAUSE...");
    simulatedPause = false;
    pause(SESSION_STOP);
  };

  public BluetoothEllipticalManager(ManagerEventListener mListener,
                                    DCEllipticalTrainer ellipticalTrainer, float weight, Boolean isMetric) {
    this.ellipticalTrainer = ellipticalTrainer;
    this.mListener = mListener;
    this.isMetric = isMetric;
    initialize(ellipticalTrainer, true);
    equipmentDataMonitor = new BikeMonitor(this);
    this.weight = weight;
  }

  public void initialize(DCEllipticalTrainer ellipticalBike, final boolean clearData) {
    Timber.i("BLUETOOTH MANAGER DOMYOS INITIALIZE EQUIPMENT");
    this.clearData = clearData;
    if (!clearData) {
      lastKnownCumulativeDistance = bluetoothSportStats.getCurrentSessionCumulativeKM();
      lastKnownCumulativeKcal = bluetoothSportStats.getKcalPerHour();
      lastKcalObtainedFromEquipment = 0;
    }
    ellipticalTrainer = ellipticalBike;
        Timber.i(
            "BLUETOOTH MANAGER DOMYOS -----------------------ELLIPTICAL BEGIN--------------------");
    //Set equipment mode to workout to prepare practice
    if (ellipticalTrainer.getMode() != DCEquipment.DCEquipmentModeWorkout) {
            Timber.i("BLUETOOTH MANAGER DOMYOS ASK WORKOUT...");
      ellipticalTrainer.setMode(DCEquipment.DCEquipmentModeWorkout,
          dcEquipment -> onWorkoutCompletion(clearData), genericErrorBlock);
    } else {
            Timber.i("BLUETOOTH MANAGER DOMYOS ALREADY WORKOUT");
      onWorkoutCompletion(clearData);
    }
        Timber.i("BLUETOOTH MANAGER DOMYOS REGISTER EQUIPMENT EVENT LISTENER");
    //listen to console and practice callbacks
    ellipticalTrainer.setListener(this);
    ellipticalTrainer.getSportData().setListener(this);
  }


  /**
   * Interact with equipment only in workout mode
   */
  private void onWorkoutCompletion(final boolean clearData) {
    Timber.i("BLUETOOTH MANAGER DOMYOS IN WORKOUT MODE !");
    //Get the equipment id
    Timber.i("BLUETOOTH MANAGER DOMYOS ASK EQUIPMENT ID");
    if(isConnected(ellipticalTrainer)) {
      ellipticalTrainer.getEquipmentID((dcEquipment, s) -> onIdReceived(s, clearData),
          idErrorBlock);
    }
  }

  private void onIdReceived(String s, boolean clearData){
    Timber.i("BLUETOOTH MANAGER DOMYOS EQUIPMENT ID RECEIVED : %s", s);
    try {
      equipmentID = Integer.valueOf(s);
    }catch (NumberFormatException e){
      equipmentID = DEFAULT_EQUIPMENT_SAFE_UNKNOWN_ID_REPLACEMENT;
            Timber.e(e);
    }
    mListener.onEquipmentIdReceived(s);

    //Get equipment version number
    Timber.i("BLUETOOTH MANAGER DOMYOS ASK EQUIPMENT INFO ...");
    if(isConnected(ellipticalTrainer)) {
      ellipticalTrainer.getEquipmentInfo(
          (equipment, dcEquipmentInfo) -> onEquipmentInfoReceived(s, dcEquipmentInfo, clearData),
          infoErrorBlock);
    }else{
      Timber.i("BLUETOOTH MANAGER DOMYOS EQUIPMENT INFO CALL : NOT CONNECTED, NO MORE CALLS");
    }
  }

  private void onEquipmentInfoReceived(String s, DCEquipmentInfo dcEquipmentInfo, boolean clearData){
        Timber.i("BLUETOOTH MANAGER DOMYOS EQUIPMENT INFO RECEIVED");
    if (clearData) {
      computeKcal();
      bluetoothSportStats.setCurrentSessionCumulativeKM(
          ellipticalTrainer.getSportData().getCurrentSessionCumulativeKM());
    } else {
      equipmentDataMonitor.resumePause();
    }
    bluetoothSportStats.setResistance(
        ellipticalTrainer.getSportData().getTorqueResistanceLevel());
    bluetoothSportStats.setAnalogHeartRate(
        ellipticalTrainer.getSportData().getAnalogHeartRate());
    bluetoothSportStats.setRpm(ellipticalTrainer.getSportData().getCurrentRPM());
    bluetoothSportStats.setSpeedKmPerHour(
        ellipticalTrainer.getSportData().getCurrentSpeedKmPerHour());
    bluetoothSportStats.setSessionAverageSpeedChanged(
        ellipticalTrainer.getSportData().getCurrentSessionAverageSpeed());
    mListener.onSportDataReceived(bluetoothSportStats);
    if (dcEquipmentInfo != null) {
      equipmentVersion = dcEquipmentInfo.getFirmwareVersion();
      equipmentInfo.setFirmwareVersion(dcEquipmentInfo.getFirmwareVersion());
      equipmentInfo.setSerialNumber(dcEquipmentInfo.getSerialNumber());
      if(BluetoothEquipmentConsoleUtils.consoleModelIdMap.get(equipmentID)!=null) {
        equipmentInfo.setModelId(BluetoothEquipmentConsoleUtils.consoleModelIdMap.get(equipmentID));
      }
    }
    if (mListener != null) {
      mListener.onEquipmentInfoReceived(equipmentInfo);
    }

    //clear session data
        Timber.i("BLUETOOTH MANAGER DOMYOS ASK CLEAR SESSION DATA ...");
    if (clearData) {
      if(isConnected(ellipticalTrainer)) {
        ellipticalTrainer.setSessionData(DCEquipment.DCSessionDataTypeClear, dcEquipment1 -> Timber.i("BLUETOOTH MANAGER DOMYOS SESSION DATA CLEARED !"),
            (dcEquipment, dcError) -> Timber.i(
                "BLUETOOTH MANAGER DOMYOS SESSION DATA CLEAR --------> ERROR"));
      }
    } else {
      torqueResistanceLevel = (int) bluetoothSportStats.getResistance();
      setResistance(bluetoothSportStats.getResistance());
    }
    equipmentInfo.setMaxResistance(RESISTANCE_MAX);
    equipmentInfo.setMinResistance(RESISTANCE_MIN);
    displayBluetoothIcon();
    BluetoothEquipmentConsoleUtils.initConsoleDisplay(TypeConstants.TYPE_SPORT_ELLIPTIC,
        ellipticalTrainer, equipmentID, equipmentVersion, started, dcEquipment -> Timber.i(
            "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM INFO RECEIVED --------> success"),
        (dcEquipment, dcError) -> Timber.i(
            "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM INFO RECEIVED --------> ERROR"), clearData,
        bluetoothSportStats.getCurrentSessionCumulativeKM(), bluetoothSportStats.getKcalPerHour());
    Timber.i("BLUETOOTH MANAGER DOMYOS END ELLIPTICAL INITIALIZATION");
  }

  /**
   * send hotkey commands to equipments
   */
  private void setHotKey(int hotkey) {
    if(isConnected(ellipticalTrainer)) {
      ellipticalTrainer.setHotKey(hotkey, dcEquipment -> Timber.i("BLUETOOTH MANAGER SET PAUSE/START STATE --------> success"),
          (dcEquipment, dcError) -> Timber.i(
              "BLUETOOTH MANAGER SET PAUSE/START STATE --------> ERROR"));
    } else {
      Timber.i("BLUETOOTH MANAGER SET PAUSE/START STATE --------> NOT CONNECTED");
    }
  }

  /**
   * Display the bluetooth icon on the elliptical console
   */
  private void displayBluetoothIcon() {
    infoParams.setBtLedSwitch(true);
    sendWorkoutInfoParamsToEquipment(infoParams);
  }



  /**
   * User interactions with console buttons
   */
  private void handleButtonTap(int buttonIndex) {
    torqueResistanceLevel = ellipticalTrainer.getSportData().getTorqueResistanceLevel();
    fanSpeed = ellipticalTrainer.getFanSpeedLevel();
    switch (buttonIndex) {
      case DCEllipticalTrainer.DCEllipticalTrainerPressedButtonLoadPlus:
        torqueResistanceLevel++;
        setResistance(torqueResistanceLevel);
        break;
      case DCEllipticalTrainer.DCEllipticalTrainerPressedButtonLoadMinus:
        torqueResistanceLevel--;
        setResistance(torqueResistanceLevel);
        break;
      case DCEllipticalTrainer.DCEllipticalTrainerPressedButtonFanMinus:
        if (fanSpeed > 0 && isConnected(ellipticalTrainer)) {
          fanSpeed--;
          ellipticalTrainer.setFanSpeedLevel(fanSpeed, dcEquipment -> Timber.i(
              "BLUETOOTH MANAGER SET FAN SPEED DECREASE --------> success"),
              (dcEquipment, dcError) -> Timber.i(
                  "BLUETOOTH MANAGER SET FAN SPEED DECREASE --------> ERROR"));
        }
        break;
      case DCEllipticalTrainer.DCEllipticalTrainerPressedButtonFanPlus:
        fanSpeed++;
        if(fanSpeed<5 && isConnected(ellipticalTrainer)) {
          ellipticalTrainer.setFanSpeedLevel(fanSpeed, dcEquipment -> Timber.i(
              "BLUETOOTH MANAGER SET FAN SPEED INCREASE --------> success"),
              (dcEquipment, dcError) -> Timber.i(
                  "BLUETOOTH MANAGER SET FAN SPEED INCREASE --------> ERROR"));
        }
        break;
      case DCEllipticalTrainer.DCEllipticalTrainerPressedButtonQuit:
        if (!simulatedPause) {
          pause(USER_REQUEST_PAUSE);
        }
        break;
      case DCEllipticalTrainer.DCEllipticalTrainerPressedButtonStart:
        if (simulatedPause) {
          pause(USER_REQUEST_START);
        }
        break;
      default:
        break;
    }
  }

  /**
   * Change torque resistance level and display it on console
   */
  private void setTorque(int resistanceLevel) {
    if (resistanceLevel > 0) {
      infoParams.setTorqueResistanceLevel(resistanceLevel);
      infoParams.setIncline(RESISTANCE_MIN);
      sendWorkoutInfoParamsToEquipment(infoParams);
      BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_ELLIPTIC,
        ellipticalTrainer, equipmentID, equipmentVersion, DCUnit.RESISTANCE, started,
        resistanceLevel, dcEquipment -> Timber.i(
              "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM SET RESISTANCE --------> success"),
          (dcEquipment, dcError) -> Timber.i(
              "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM SET RESISTANCE --------> ERROR"));
    }
  }

  /**
   * Send values to the treadmill console
   */
  private void sendWorkoutInfoParamsToEquipment(
    DCEllipticalTrainerWorkoutModeSetInfoParameters infoParams) {
    if(isConnected(ellipticalTrainer)) {
      ellipticalTrainer.setWorkoutModeInfoValue(infoParams, dcEquipment -> Timber.i(
          "BLUETOOTH MANAGER SEND INFO PARAMS (speed, incline, bluetooth icon ...) --------> success"),
          (dcEquipment, dcError) -> Timber.i(
              "BLUETOOTH MANAGER SEND INFO PARAMS (speed, incline, bluetooth icon ...) --------> ERROR"));
    }
  }

  private void computeKcal() {
    if (equipmentID != null) {
      if (TypeConstants.contain(console3IdList, equipmentID) && equipmentVersion <= EQUIPMENT_FIRMWARE_1_5) {
        if (computationStrategy == null && TypeConstants.contain(
          ComputeKCALStrategyConsole3v1.affectedIdList, equipmentID)) {
          computationStrategy = new ComputeKCALStrategyConsole3v1();
        } else if (computationStrategy == null && TypeConstants.contain(
          ComputeKCALStrategyConsole3v2.affectedIdList, equipmentID)) {
          computationStrategy = new ComputeKCALStrategyConsole3v2();
        }
            } else {
                if (TypeConstants.contain(console4IdList, equipmentID)
                    && equipmentVersion < CONSOLE_4_NEW_VERSION) {
        computationStrategy = new ComputeKCALStrategyConsole3v2();
      }
            }

      if (TypeConstants.contain(console3IdList, equipmentID) && equipmentVersion <= EQUIPMENT_FIRMWARE_1_5
        || TypeConstants.contain(console4IdList, equipmentID)
                && computationStrategy != null
        && equipmentVersion
                < CONSOLE_4_NEW_VERSION) {//FIXME mettre dans method priver commune au if elseif precedent

        bluetoothSportStats.setKcalPerHour((float) (bluetoothSportStats.getKcalPerHour()
          + (computationStrategy.computeKCALValue(this) /3600f)));
      }
    }
  }

    /**
     * Initiate program beginning
     */
    public void startProgram() {
        //listen to console and practice callbacks
        started = true;
        displayBluetoothIcon();
     /*   BluetoothEquipmentConsoleUtils.initializeZoneInformations(TypeConstants.TYPE_SPORT_ELLIPTIC,
            ellipticalTrainer, equipmentID, equipmentVersion, started, 0, dcEquipment -> Timber.i(
                "BLUETOOTH MANAGER SEND RESET TO 0 DISPLAY FROM START PROGRAM --------> success"),
            (dcEquipment, dcError) -> Timber.i(
                "BLUETOOTH MANAGER SEND RESET TO 0 DISPLAY FROM START PROGRAM --------> ERROR"));*/
        simulatedPause = false;
      pause(SESSION_START);
       /* if (ellipticalTrainer.getTabOnEquipment()) {
            pause(SESSION_START);
        } else {
            pause(TAB_NOT_DETECTED);
        }*/
        equipmentDataMonitor.startMonitoring();
    }

    @Override public void stopProgram() {
        started = false;
        BluetoothEquipmentConsoleUtils.displayMainMessage(TypeConstants.TYPE_SPORT_ELLIPTIC,
            ellipticalTrainer, equipmentID, equipmentVersion, false, dcEquipment -> Timber.i(
                "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM STOP PROGRAM --------> success"),
            (dcEquipment, dcError) -> Timber.i(
                "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM STOP PROGRAM --------> ERROR"));
        clearSessionData();
        equipmentDataMonitor.stopMonitoring();
    }

    @Override public boolean isProgramStarted() {
        return started;
    }

    @Override public void notifyManager() {
        if (mListener != null) {
            computeKcal();
            mListener.onSportDataReceived(bluetoothSportStats);
        }
    }

  //console section
  @Override public void equipmentTabOnEquipmentChanged(DCEquipment dcEquipment, boolean b) {
    if (ellipticalTrainer.getTabOnEquipment() && isConnected(ellipticalTrainer)) {
      BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_ELLIPTIC,
        ellipticalTrainer, equipmentID, equipmentVersion, DCUnit.CURRENT_HEART_RATE, started, 0,
          equipment -> Timber.i(
              "BLUETOOTH MANAGER SEND DISPLAY FROM TAB CHANGED --------> success"),
          (equipment, dcError) -> Timber.i(
              "BLUETOOTH MANAGER SEND DISPLAY FROM TAB CHANGED --------> ERROR"));
    }
    if (!ellipticalTrainer.getTabOnEquipment()) {
      pause(TAB_NOT_DETECTED);
    }

    if (ellipticalTrainer.getTabOnEquipment()) {
      pause(TAB_DETECTED);
    }
    if (mListener != null) {
      mListener.onEquipmentTabChange(ellipticalTrainer.getTabOnEquipment());
    }
  }

  @Override public void equipmentErrorOccurred(DCEquipment dcEquipment, int i) {

  }

  @Override public void equipmentPressedButtonChanged(DCEquipment dcEquipment, int i) {
    handleButtonTap(i);
  }

  @Override public void equipmentOnHotKeyStatusChanged(DCEquipment dcEquipment, int i) {
    //not used
  }

  @Override public void equipmentOnFanSpeedLevelChanged(DCEquipment dcEquipment, int i) {
    handleButtonTap(i);
  }

  //sport data section
  @Override public void onWattChanged(float v) {
    LogUtils.e("===============onWattChanged=================="+v);
    bluetoothSportStats.setWatt(v);
    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_WATT,bluetoothSportStats));
    notifyManager();
  }

  @Override public void onCurrentRPMChanged(int i) {
    LogUtils.e("===============onCurrentRPMChanged=================="+i);

    currentRPM = i;
    equipmentDataMonitor.dataReceived(DCUnit.CURRENT_ROTATION, currentRPM);
    bluetoothSportStats.setRpm(i);
    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_RPM,bluetoothSportStats));

    if(started) {
      BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_ELLIPTIC,
          ellipticalTrainer, equipmentID, equipmentVersion, DCUnit.CURRENT_ROTATION, true, i,
          equipment -> Timber.i(
              "BLUETOOTH MANAGER SEND CURRENT_ROTATION DISPLAY FROM RPM CHANGED --------> success"),
          (equipment, dcError) -> Timber.i(
              "BLUETOOTH MANAGER SEND CURRENT_ROTATION DISPLAY FROM RPM CHANGED --------> ERROR"));
    }
    notifyManager();
  }

  @Override public void onTorqueResistanceLevelChanged(int i) {
    LogUtils.e("===============onTorqueResistanceLevelChanged=================="+i);

    torqueResistanceLevel = i;
    bluetoothSportStats.setResistance(i);
    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_RESISTANCE,bluetoothSportStats));

    BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_ELLIPTIC,
      ellipticalTrainer, equipmentID, equipmentVersion, DCUnit.RESISTANCE, started, i,
        equipment -> Timber.i(
            "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM RESISTANCE CHANGED --------> success"),
        (equipment, dcError) -> Timber.i(
            "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM RESISTANCE CHANGED --------> ERROR"));
    notifyManager();
  }

  @Override
  public void onInclineChanged(int incline) {
    LogUtils.e("===============onCurrentRPMChanged=================="+incline);

    bluetoothSportStats.setInclinePercentage(incline);
    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_INCLINE,bluetoothSportStats));

    Timber.i(
            "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM RESISTANCE CHANGED --------> onInclineChanged");
  }

  @Override public void onCurrentSpeedKmPerHourChanged(float v) {
    bluetoothSportStats.setSpeedKmPerHour(v);
    LogUtils.e("===============onCurrentRPMChanged=================="+v);

    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_SPEED,bluetoothSportStats));

    BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_ELLIPTIC,
      ellipticalTrainer, equipmentID, equipmentVersion, DCUnit.CURRENT_SPEED, started,
      bluetoothSportStats.getSpeedKmPerHour(), equipment -> Timber.i(
            "BLUETOOTH MANAGER SEND SPEED DISPLAY FROM SPEED CHANGED --------> success"),
        (equipment, dcError) -> Timber.i(
            "BLUETOOTH MANAGER SEND SPEED DISPLAY FROM SPEED CHANGED --------> ERROR"));
    notifyManager();
  }

  @Override public void onCurrentSessionCumulativeKCalChanged(int i) {
    LogUtils.e("===============onCurrentSessionCumulativeKCalChanged=================="+i);

    computeKcal();
    bluetoothSportStats.setKcalPerHour(i);

    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_CALORIE,bluetoothSportStats));

    if (equipmentVersion > EQUIPMENT_FIRMWARE_1_5
      || equipmentID!=null && equipmentVersion >= BluetoothEquipmentConsoleUtils.consoleNewDisplayIdVersionMap.get(
      equipmentID)
      || computationStrategy == null && equipmentID != null) {
      if(lastKcalObtainedFromEquipment==999f && i==0){
        lastKnownCumulativeKcal +=1000f;
      }
      bluetoothSportStats.setKcalPerHour(i + lastKnownCumulativeKcal);
    }
    lastKcalObtainedFromEquipment = i;
    if(started) {
      BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_ELLIPTIC,
          ellipticalTrainer, equipmentID, equipmentVersion, DCUnit.KCAL_BURNT, true,
          bluetoothSportStats.getKcalPerHour(), equipment -> Timber.i(
              "BLUETOOTH MANAGER SEND CALORIES DISPLAY FROM CALORIES CHANGED --------> success"), (equipment, dcError) -> Timber.i(
              "BLUETOOTH MANAGER SEND CALORIES DISPLAY FROM CALORIES CHANGED --------> ERROR"));
    }
    notifyManager();
  }

  @Override public void onCurrentSessionCumulativeKMChanged(float v) {
    LogUtils.e("===============onCurrentSessionCumulativeKMChanged=================="+v);

    bluetoothSportStats.setCurrentSessionCumulativeKM(v + lastKnownCumulativeDistance);
    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_DIS,bluetoothSportStats));

    BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_ELLIPTIC,
      ellipticalTrainer, equipmentID, equipmentVersion, DCUnit.DISTANCE, started,
      bluetoothSportStats.getCurrentSessionCumulativeKM(), equipment -> Timber.i(
            "BLUETOOTH MANAGER SEND DISTANCE DISPLAY FROM DISTANCE CHANGED --------> success"),
        (equipment, dcError) -> Timber.i(
            "BLUETOOTH MANAGER SEND DISTANCE DISPLAY FROM DISTANCE CHANGED --------> ERROR"));
    notifyManager();
  }

  @Override public void onAnalogHeartRateChanged(int i) {
    LogUtils.e("===============onAnalogHeartRateChanged=================="+i);

    bluetoothSportStats.setAnalogHeartRate(i);
    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_HEART_RATE,bluetoothSportStats));

    BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_ELLIPTIC,
      ellipticalTrainer, equipmentID, equipmentVersion, DCUnit.CURRENT_HEART_RATE, started, i,
        equipment -> Timber.i(
            "BLUETOOTH MANAGER SEND HEART RATE DISPLAY FROM HEART RATE CHANGED --------> success"),
        (equipment, dcError) -> Timber.i(
            "BLUETOOTH MANAGER SEND HEART RATE DISPLAY FROM HEART RATE CHANGED --------> ERROR"));
    notifyManager();
  }

  @Override public void onCurrentSessionAverageSpeedChanged(float v) {
    LogUtils.e("===============onCurrentSessionAverageSpeedChanged=================="+v);

    bluetoothSportStats.setSessionAverageSpeedChanged(v);
    EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_AVG_SPEED,bluetoothSportStats));

    notifyManager();
  }

  @Override public void onCountChanged(int i) {
    notifyManager();
  }

  @Override public BluetoothSportStats getBluetoothSportStats() {
    return bluetoothSportStats;
  }

  @Override public float getWeight() {
    return weight;
  }

  @Override public DCEquipment getEquipment() {
    return ellipticalTrainer;
  }

  @Override public boolean isStarted() {
    return ellipticalTrainer.getHotKeyStatus() == DCEquipment.DCHotKeyStart;
  }

  @Override public EquipmentInfo getEquipmentInfo() {
    return equipmentInfo;
  }

  @Override public boolean isOnTab() {
    return ellipticalTrainer.getTabOnEquipment();
  }

  @Override public void clearSessionData() {
    setResistance(RESISTANCE_MIN);
    computeKcal();
    lastKnownCumulativeKcal = 0f;
    lastKcalObtainedFromEquipment = 0f;
    lastKnownCumulativeDistance = 0f;

    bluetoothSportStats.setKcalPerHour(0);
    bluetoothSportStats.setAnalogHeartRate(0);
    bluetoothSportStats.setCurrentSessionCumulativeKM(0);
    bluetoothSportStats.setSessionAverageSpeedChanged(0);
    bluetoothSportStats.setSpeedKmPerHour(ellipticalTrainer.getSportData().getCurrentSpeedKmPerHour());
    if(isConnected(ellipticalTrainer)) {
      ellipticalTrainer.setSessionData(DCEquipment.DCSessionDataTypeClear, sessionStopCompletionBlock,
          (dcEquipment, dcError) -> Timber.i(
              "BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ASKED --------> ERROR"));
    } else {
      Timber.i("BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ASKED --------> NOT CONNECTED");
    }
  }

  @Override public void setResistance(float value) {
    torqueResistanceLevel = (int) value;
    if (torqueResistanceLevel > RESISTANCE_MIN && torqueResistanceLevel < RESISTANCE_MAX) {
      setTorque(torqueResistanceLevel);
    }

    if (torqueResistanceLevel <= RESISTANCE_MIN) {
      torqueResistanceLevel = RESISTANCE_MIN;
      setTorque(RESISTANCE_MIN);
    }

    if (torqueResistanceLevel >= RESISTANCE_MAX) {
      torqueResistanceLevel = RESISTANCE_MAX;
      setTorque(RESISTANCE_MAX);
    }
  }

  @Override public void setSpeedCmd(float value) {
    //do nothing
  }

  @Override public void clearSportDataListeners() {
    if (ellipticalTrainer != null) {
      ellipticalTrainer.setListener(null);
      ellipticalTrainer.getSportData().setListener(null);
    }
  }

  @Override public void pauseClicked(PauseCauseEnum pauseCauseEnum) {
    pause(pauseCauseEnum);
  }

  /**
   * Function used to switch between pause and start equipment
   */
  private void pause(PauseCauseEnum pauseReason) {
    if (started
      && ellipticalTrainer.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected
      && (simulatedPause || SESSION_START == pauseReason)
    //  && ellipticalTrainer.getTabOnEquipment()
      && pauseReason != TAB_NOT_DETECTED) {
      setHotKey(DCEquipment.DCHotKeyStart);
      ellipticalTrainer.setFanSpeedLevel(fanSpeed, genericCompletionBlock, genericErrorBlock);
      simulatedPause = false;
      mListener.onEquipmentPause(new EquipmentPauseState(false, pauseReason));
      equipmentDataMonitor.resumePause();
    } else {
      if (!simulatedPause) {
        simulatedPause = true;
        if (pauseReason == USER_REQUEST_PAUSE) {
          equipmentDataMonitor.pauseRequested();
        }
        if (pauseReason == SESSION_STOP || pauseReason == TAB_NOT_DETECTED) {
          setHotKey(DCEquipment.DCHotKeyPause);
        }
        mListener.onEquipmentPause(new EquipmentPauseState(true, pauseReason));
      }
    }
  }

  @Override public void notifyRpmPause() {
    if (!simulatedPause) {
      pause(EQUIPMENT_INACTIVITY);
    }
  }

  @Override public void notifyRpmStart() {
    if (simulatedPause) {
      pause(EQUIPMENT_ACTIVITY);
    }
  }

  @Override public float getMonitoredBikeRPM() {
    return currentRPM;
  }
}
