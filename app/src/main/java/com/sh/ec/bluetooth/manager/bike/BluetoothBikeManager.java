package com.sh.ec.bluetooth.manager.bike;

import com.appdevice.domyos.DCBike;
import com.appdevice.domyos.DCCompletionBlock;
import com.appdevice.domyos.DCCompletionBlockWithError;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentInfo;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.parameters.bike.DCBikeWorkoutModeSetInfoParameters;
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
import com.sh.ec.bluetooth.manager.utils.TypeConstants;
import com.sh.ec.entity.EquipmentInfo;
import com.sh.ec.entity.PauseCauseEnum;
import com.sh.ec.event.SportDataChangeEvent;
import com.sh.ec.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static com.sh.ec.bluetooth.manager.BluetoothManager.EQUIPMENT_WORKOUT_ERROR_CODE;
import static com.sh.ec.bluetooth.manager.BluetoothManagerDemo.EQUIPMENT_FIRMWARE_1_5;
import static com.sh.ec.bluetooth.manager.utils.DCUnit.CURRENT_HEART_RATE;
import static com.sh.ec.bluetooth.manager.utils.DCUnit.CURRENT_ROTATION;
import static com.sh.ec.bluetooth.manager.utils.DCUnit.CURRENT_SPEED;
import static com.sh.ec.bluetooth.manager.utils.DCUnit.DISTANCE;
import static com.sh.ec.bluetooth.manager.utils.DCUnit.KCAL_BURNT;
import static com.sh.ec.bluetooth.manager.utils.DCUnit.RESISTANCE;
import static com.sh.ec.entity.PauseCauseEnum.EQUIPMENT_ACTIVITY;
import static com.sh.ec.entity.PauseCauseEnum.EQUIPMENT_INACTIVITY;
import static com.sh.ec.entity.PauseCauseEnum.SESSION_START;
import static com.sh.ec.entity.PauseCauseEnum.SESSION_STOP;
import static com.sh.ec.entity.PauseCauseEnum.TAB_DETECTED;
import static com.sh.ec.entity.PauseCauseEnum.TAB_NOT_DETECTED;
import static com.sh.ec.entity.PauseCauseEnum.USER_REQUEST_PAUSE;
import static com.sh.ec.entity.PauseCauseEnum.USER_REQUEST_START;
import static com.sh.ec.service.MangerService.isConnected;

/**
 * Class used to manage interactions with bike
 * <p>
 * Created by mbouchagour on 12/05/2017.
 */
public class BluetoothBikeManager
        implements BikeListener, BluetoothEquipmentSpecificManager, MonitoredBike {

    public static final int RESISTANCE_MAX = 15;
    private static final int RESISTANCE_MIN = 1;

    private static final int DEFAULT_WORKOUT_RESEND_DELAY = 2000;

    private int DEFAULT_EQUIPMENT_SAFE_UNKNOWN_ID_REPLACEMENT = 8363074;

    private static final int[] console3IdList =
            AppContext.getInstance().getResources().getIntArray(R.array.console3IdList);
    private static final int[] console4IdList =
            AppContext.getInstance().getResources().getIntArray(R.array.console4IdList);

    private DCBike bike;
    private BluetoothSportStats bluetoothSportStats = new BluetoothSportStats();
    private final EquipmentInfo equipmentInfo = new EquipmentInfo();
    private final DCBikeWorkoutModeSetInfoParameters infoParams =
            new DCBikeWorkoutModeSetInfoParameters();

    private ManagerEventListener mListener;
    private Boolean isMetric = true;
    private float equipmentVersion = -1f;
    private Integer equipmentID;

    private float weight = 0;
    private ComputeKCALStrategy computationStrategy = null;
    private EquipmentDataMonitor equipmentDataMonitor;

    private int fanSpeed = 0;
    private int torqueResistanceLevel = 1;

    //pause when no sport data received for long time
    private boolean simulatedPause = true;
    private boolean clearData = false;

    private float lastKnownCumulativeDistance = 0f;
    private float lastKnownCumulativeKcal = 0f;
    private float lastKcalObtainedFromEquipment = 0f;

    private float currentRPM = 0;

    private final DCCompletionBlockWithError genericErrorBlock = new DCCompletionBlockWithError() {
        @Override
        public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
            Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
                    dcEquipment.getName(), dcError.getDescription());
            if (dcError.getCode() == EQUIPMENT_WORKOUT_ERROR_CODE && dcEquipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
                //avoid to spam console by delay calls
                LogUtils.d("BLUETOOTH MANAGER DOMYOS WORKOUT ERROR OCCURRED, TRYING DELAYED CALL ...");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isConnected(bike)) {
                            //Switch equipment to workout mode for practice
                            if (bike.getMode() != DCEquipment.DCEquipmentModeWorkout) {
                                LogUtils.d("... BLUETOOTH MANAGER DOMYOS WORKOUT DELAYED CALL");
                                bike.setMode(DCEquipment.DCEquipmentModeWorkout, dcEquipment1 -> onWorkoutCompletion(!isProgramStarted()),
                                        genericErrorBlock);
                            } else {
                                LogUtils.d("... BLUETOOTH MANAGER DOMYOS WORKOUT DELAYED CALL : ALREADY IN WORKOUT MODE");
                                onWorkoutCompletion(!isProgramStarted());
                            }
                        } else {
                            LogUtils.d("... BLUETOOTH MANAGER DOMYOS WORKOUT DELAYED CALL : NOT CONNECTED, NO MORE CALLS");
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
        @Override
        public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
            Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
                    dcEquipment.getName(), dcError.getDescription());
            if (dcEquipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
                LogUtils.d("BLUETOOTH MANAGER DOMYOS ID ERROR OCCURRED, TRYING DELAYED CALL ...");
                //avoid to spam console by delay calls
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isConnected(bike)) {
                            onWorkoutCompletion(clearData);
                        } else {
                            LogUtils.d("... BLUETOOTH MANAGER DOMYOS ID DELAYED CALL : NOT CONNECTED, NO MORE CALLS");
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
     * Used to trigger equipment pause after session stop and data cleared
     */
    private final DCCompletionBlock sessionStopCompletionBlock = dcEquipment -> {
        LogUtils.d("BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ------> SUCCESS... ASKING SESSION PAUSE...");
        simulatedPause = false;
        pause(SESSION_STOP);
    };

    /**
     * Used to showView in console
     * <p>
     * TODO !!
     */
    private final DCCompletionBlock genericCompletionBlock = dcEquipment -> {
        //empty
    };
    private boolean started = false;

    public BluetoothBikeManager(ManagerEventListener listener, final DCBike bike, final float weight,
                                final Boolean isMetric) {
        this.bike = bike;
        this.mListener = listener;
        this.isMetric = isMetric;
        initialize(bike, true);
        equipmentDataMonitor = new BikeMonitor(this);
        this.weight = weight;

        LogUtils.e("---sh-================"+bike.getTabOnEquipment());
    }

    public void initialize(DCBike connectedEquipment, final boolean clearData) {
        LogUtils.d("BLUETOOTH MANAGER DOMYOS INITIALIZE EQUIPMENT");
        this.clearData = clearData;
        if (!clearData) {
            lastKnownCumulativeDistance = bluetoothSportStats.getCurrentSessionCumulativeKM();
            lastKnownCumulativeKcal = bluetoothSportStats.getKcalPerHour();
            lastKcalObtainedFromEquipment = 0;
        }
        //Switch equipment to workout mode for practice
        bike = connectedEquipment;
        if (bike.getMode() != DCEquipment.DCEquipmentModeWorkout) {
            LogUtils.d("BLUETOOTH MANAGER DOMYOS INITIALIZE EQUIPMENT : ASK WORKOUT MODE ...");
            bike.setMode(DCEquipment.DCEquipmentModeWorkout,
                    dcEquipment -> onWorkoutCompletion(clearData), this.genericErrorBlock);
        } else {
            LogUtils.d("BLUETOOTH MANAGER DOMYOS INITIALIZE EQUIPMENT : ALREADY IN WORKOUT MODE");
            onWorkoutCompletion(clearData);
        }
        //register to sport callbacks events
        bike.setListener(this);
        bike.getSportData().setListener(this);
    }

    /**
     * Interact with bike only in workout mode
     */
    private void onWorkoutCompletion(final boolean clearData) {
        LogUtils.d("BLUETOOTH MANAGER DOMYOS IN WORKOUT MODE !");
        //Get the equipment id
        LogUtils.d("BLUETOOTH MANAGER DOMYOS ASK EQUIPMENT ID");
        if (isConnected(bike)) {
            bike.getEquipmentID((dcEquipment, s) -> onIdReceived(s, clearData), idErrorBlock);
        }
    }


    private void onIdReceived(String s, boolean clearData) {
        LogUtils.d("BLUETOOTH MANAGER DOMYOS EQUIPMENT ID RECEIVED : %s"+ s);
        try {
            equipmentID = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            equipmentID = DEFAULT_EQUIPMENT_SAFE_UNKNOWN_ID_REPLACEMENT;
            Timber.e(e);
        }
        mListener.onEquipmentIdReceived(s);

        //Get equipment version number
        LogUtils.d("BLUETOOTH MANAGER DOMYOS ASK EQUIPMENT INFO ...");
        if (isConnected(bike)) {
            bike.getEquipmentInfo(
                    (dcEquipment, dcEquipmentInfo) -> onEquipmentInfoReceived(s, dcEquipmentInfo, clearData),
                    (dcEquipment, dcError) -> LogUtils.d(
                            "BLUETOOTH MANAGER DOMYOS ASK EQUIPMENT INFO ...--------> ERROR"));
        }
    }

    private void onEquipmentInfoReceived(String s, DCEquipmentInfo dcEquipmentInfo, boolean clearData) {
        LogUtils.d("BLUETOOTH MANAGER DOMYOS EQUIPMENT INFO RECEIVED");
        if (clearData) {
            computeKcal();
            bluetoothSportStats.setCurrentSessionCumulativeKM(
                    bike.getSportData().getCurrentSessionCumulativeKM());
        } else {
            equipmentDataMonitor.resumePause();
            equipmentTabOnEquipmentChanged(bike, bike.getTabOnEquipment());
        }
        bluetoothSportStats.setResistance(bike.getSportData().getTorqueResistanceLevel());
        bluetoothSportStats.setAnalogHeartRate(bike.getSportData().getAnalogHeartRate());

        bluetoothSportStats.setRpm(bike.getSportData().getCurrentRPM());
        bluetoothSportStats.setSessionAverageSpeedChanged(
                bike.getSportData().getCurrentSessionAverageSpeed());
        bluetoothSportStats.setSpeedKmPerHour(bike.getSportData().getCurrentSpeedKmPerHour());
        mListener.onSportDataReceived(bluetoothSportStats);
        if (dcEquipmentInfo != null) {
            equipmentVersion = dcEquipmentInfo.getFirmwareVersion();
            equipmentInfo.setFirmwareVersion(dcEquipmentInfo.getFirmwareVersion());
            equipmentInfo.setSerialNumber(dcEquipmentInfo.getSerialNumber());
            if (BluetoothEquipmentConsoleUtils.consoleModelIdMap.get(equipmentID) != null) {
                equipmentInfo.setModelId(BluetoothEquipmentConsoleUtils.consoleModelIdMap.get(equipmentID));
            }
        }
        if (mListener != null) {
            mListener.onEquipmentInfoReceived(equipmentInfo);
        }

        //clear session data
        if (clearData) {
            LogUtils.d("BLUETOOTH MANAGER DOMYOS ASK CLEAR SESSION DATA ...");
            if (isConnected(bike)) {
                bike.setSessionData(DCEquipment.DCSessionDataTypeClear, dcEquipment1 -> LogUtils.d("BLUETOOTH MANAGER DOMYOS SESSION DATA CLEARED !"),
                        (dcEquipment, dcError) -> LogUtils.d(
                                "BLUETOOTH MANAGER DOMYOS SESSION DATA CLEAR --------> ERROR"));
            }
        } else {
            torqueResistanceLevel = (int) bluetoothSportStats.getResistance();
            setResistance(bluetoothSportStats.getResistance());
        }
        equipmentInfo.setMaxResistance(RESISTANCE_MAX);
        equipmentInfo.setMinResistance(RESISTANCE_MIN);
        displayBluetoothIcon();
        BluetoothEquipmentConsoleUtils.initConsoleDisplay(TypeConstants.TYPE_SPORT_BIKE, bike,
                equipmentID, equipmentVersion, started, dcEquipment -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM INFO RECEIVED --------> success"),
                (dcEquipment, dcError) -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM INFO RECEIVED --------> ERROR"), clearData,
                bluetoothSportStats.getCurrentSessionCumulativeKM(), bluetoothSportStats.getKcalPerHour());

        LogUtils.d("BLUETOOTH MANAGER DOMYOS END BIKE INITIALIZATION");
    }

    /**
     * send hotkey commands to equipments
     */
    private void setHotKey(int hotKey) {
        if (isConnected(bike)) {
            bike.setHotKey(hotKey, dcEquipment -> LogUtils.d("BLUETOOTH MANAGER SET PAUSE/START STATE --------> success"),
                    (dcEquipment, dcError) -> LogUtils.d(
                            "BLUETOOTH MANAGER SET PAUSE/START STATE --------> ERROR"));
        } else {
            LogUtils.d("BLUETOOTH MANAGER SET PAUSE/START STATE --------> NOT CONNECTED");
        }
    }

    /**
     * Display the bluetooth icon on the bike console
     */
    private void displayBluetoothIcon() {
        infoParams.setBtLedSwitch(true);
        sendWorkoutInfoParamsToEquipment(infoParams);
    }

    /**
     * User interactions with console buttons
     */
    private void handleButtonTap(int buttonIndex) {
        torqueResistanceLevel = bike.getSportData().getTorqueResistanceLevel();
        fanSpeed = bike.getFanSpeedLevel();
        switch (buttonIndex) {//FIXME mettre les case dans methode privé
            case DCBike.DCBikePressedButtonLoadPlus:
                torqueResistanceLevel++;
                setResistance(torqueResistanceLevel);
                break;
            case DCBike.DCBikePressedButtonLoadMinus:
                torqueResistanceLevel--;
                setResistance(torqueResistanceLevel);
                break;
            case DCBike.DCBikePressedButtonFanMinus:
                if (fanSpeed > 0 && isConnected(bike)) {
                    fanSpeed--;
                    bike.setFanSpeedLevel(fanSpeed, dcEquipment -> LogUtils.d(
                            "BLUETOOTH MANAGER SET FAN SPEED DECREASE --------> success"),
                            (dcEquipment, dcError) -> LogUtils.d(
                                    "BLUETOOTH MANAGER SET FAN SPEED DECREASE --------> ERROR"));
                }
                break;
            case DCBike.DCBikePressedButtonFanPlus:
                fanSpeed++;
                if (fanSpeed < 5 && isConnected(bike)) {
                    bike.setFanSpeedLevel(fanSpeed, dcEquipment -> LogUtils.d(
                            "BLUETOOTH MANAGER SET FAN SPEED INCREASE --------> success"),
                            (dcEquipment, dcError) -> LogUtils.d(
                                    "BLUETOOTH MANAGER SET FAN SPEED INCREASE --------> ERROR"));
                }
                break;
            case DCBike.DCBikePressedButtonQuit:
                if (!simulatedPause) {
                    pause(USER_REQUEST_PAUSE);
                }
                break;
            case DCBike.DCBikePressedButtonStart:
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
            sendWorkoutInfoParamsToEquipment(infoParams);
            BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                    equipmentID, equipmentVersion, RESISTANCE, started, resistanceLevel,
                    dcEquipment -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM SET RESISTANCE --------> success"),
                    (dcEquipment, dcError) -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM SET RESISTANCE --------> ERROR"));
        }
    }

    /**
     * Send values to the bike console
     */
    private void sendWorkoutInfoParamsToEquipment(DCBikeWorkoutModeSetInfoParameters infoParams) {
        if (isConnected(bike)) {
            bike.setWorkoutModeInfoValue(infoParams, dcEquipment -> LogUtils.d(
                    "BLUETOOTH MANAGER SEND INFO PARAMS (speed, incline, bluetooth icon ...) --------> success"),
                    (dcEquipment, dcError) -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND INFO PARAMS (speed, incline, bluetooth icon ...) --------> ERROR"));
        }
    }

    private void computeKcal() {
        //FIXME combiner les if
        if (equipmentID != null) {
            if (TypeConstants.contain(console3IdList, equipmentID) && equipmentVersion <= EQUIPMENT_FIRMWARE_1_5) {
                if (computationStrategy == null && TypeConstants.contain(
                        ComputeKCALStrategyConsole3v1.affectedIdList, equipmentID)) {
                    computationStrategy = new ComputeKCALStrategyConsole3v1();
                } else if (computationStrategy == null && TypeConstants.contain(
                        ComputeKCALStrategyConsole3v2.affectedIdList, equipmentID)) {
                    computationStrategy = new ComputeKCALStrategyConsole3v2();
                }
            } else if (TypeConstants.contain(console4IdList, equipmentID) && equipmentVersion <= 1.0) {
                computationStrategy = new ComputeKCALStrategyConsole3v2();
            }

            if (TypeConstants.contain(console3IdList, equipmentID) && equipmentVersion <= EQUIPMENT_FIRMWARE_1_5
                    || TypeConstants.contain(console4IdList, equipmentID)
                    && computationStrategy != null
                    && equipmentVersion
                    <= 1.0) {//FIXME mettre dans method priver commune au if elseif precedent
                bluetoothSportStats.setKcalPerHour((float) (bluetoothSportStats.getKcalPerHour()
                        + (computationStrategy.computeKCALValue(this) / 3600f)));
            }
        }
    }

    /**
     * Initiate program beginning
     */
    public void startProgram() {
        started = true;
        displayBluetoothIcon();
        BluetoothEquipmentConsoleUtils.displayMainMessage(TypeConstants.TYPE_SPORT_BIKE, bike,
                equipmentID, equipmentVersion, started, dcEquipment -> Timber.i(
                        "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM START PROGRAM --------> success"),
                (dcEquipment, dcError) -> Timber.i(
                        "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM START PROGRAM --------> ERROR"));
        BluetoothEquipmentConsoleUtils.initializeZoneInformations(TypeConstants.TYPE_SPORT_BIKE,
                bike, equipmentID, equipmentVersion, started, 0, dcEquipment -> Timber.i(
                        "BLUETOOTH MANAGER SEND RESET TO 0 DISPLAY FROM START PROGRAM --------> success"),
                (dcEquipment, dcError) -> Timber.i(
                        "BLUETOOTH MANAGER SEND RESET TO 0 DISPLAY FROM START PROGRAM --------> ERROR"));
        simulatedPause = false;
        pause(SESSION_START);

     /*   if (bike.getTabOnEquipment()) {
            pause(SESSION_START);
        } else {
            pause(TAB_NOT_DETECTED);
        }*/
        equipmentDataMonitor.startMonitoring();
    }

    /**
     * Get the current bluetoothSportStat
     */
    public BluetoothSportStats getBluetoothSportStats() {
        return bluetoothSportStats;
    }

    @Override
    public void stopProgram() {
        started = false;
        BluetoothEquipmentConsoleUtils.displayMainMessage(TypeConstants.TYPE_SPORT_BIKE, bike,
                equipmentID, equipmentVersion, false, dcEquipment -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM STOP PROGRAM --------> success"),
                (dcEquipment, dcError) -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND MAIN DISPLAY FROM STOP PROGRAM --------> ERROR"));
        clearSessionData();
        equipmentDataMonitor.stopMonitoring();
    }

    @Override
    public boolean isProgramStarted() {
        return started;
    }

    @Override
    public void notifyManager() {
        if (mListener != null) {
            computeKcal();
            mListener.onSportDataReceived(bluetoothSportStats);
        }
    }

    //console interactions section
    @Override
    public void equipmentTabOnEquipmentChanged(DCEquipment dcEquipment, boolean b) {
        if (bike.getTabOnEquipment() && isConnected(bike)) {

            BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                    equipmentID, equipmentVersion, CURRENT_HEART_RATE, started, 0,
                    equipment -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND DISPLAY FROM TAB CHANGED --------> success"),
                    (equipment, dcError) -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND DISPLAY FROM TAB CHANGED --------> ERROR"));
        }

        if (!bike.getTabOnEquipment()) {
            pause(TAB_NOT_DETECTED);
        }

        if (bike.getTabOnEquipment()) {
            pause(TAB_DETECTED);
        }
        if (mListener != null) {
            mListener.onEquipmentTabChange(bike.getTabOnEquipment());
        }
    }

    @Override
    public void equipmentErrorOccurred(DCEquipment dcEquipment, int i) {

    }

    @Override
    public void equipmentPressedButtonChanged(DCEquipment dcEquipment, int i) {
        handleButtonTap(i);
    }

    @Override
    public void equipmentOnHotKeyStatusChanged(DCEquipment dcEquipment, int i) {
        //not used
    }

    @Override
    public void equipmentOnFanSpeedLevelChanged(DCEquipment dcEquipment, int i) {
        handleButtonTap(i);

    }

    //sport data section
    @Override
    public void onWattChanged(float v) {
        bluetoothSportStats.setWatt(v);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_WATT, bluetoothSportStats));

        notifyManager();
    }

    @Override
    public void onCurrentRPMChanged(int i) {
        currentRPM = i;
        equipmentDataMonitor.dataReceived(CURRENT_HEART_RATE, currentRPM);
        bluetoothSportStats.setRpm(i);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_RPM, bluetoothSportStats));

        if (started) {
            BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                    equipmentID, equipmentVersion, CURRENT_ROTATION, true, i, equipment -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND CURRENT_ROTATION DISPLAY FROM RPM CHANGED --------> success"),
                    (equipment, dcError) -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND CURRENT_ROTATION DISPLAY FROM RPM CHANGED --------> ERROR"));
        }
        notifyManager();
    }

    @Override
    public void onTorqueResistanceLevelChanged(int i) {

        torqueResistanceLevel = i;
        bluetoothSportStats.setResistance(i);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_RESISTANCE, bluetoothSportStats));

        BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                equipmentID, equipmentVersion, RESISTANCE, started, i, equipment -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM RESISTANCE CHANGED --------> success"),
                (equipment, dcError) -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND RESISTANCE DISPLAY FROM RESISTANCE CHANGED --------> ERROR"));
        notifyManager();
    }

    @Override
    public void onCurrentSpeedKmPerHourChanged(float v) {
        bluetoothSportStats.setSpeedKmPerHour(v);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_SPEED, bluetoothSportStats));

        BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                equipmentID, equipmentVersion, CURRENT_SPEED, started,
                bluetoothSportStats.getSpeedKmPerHour(), equipment -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND SPEED DISPLAY FROM SPEED CHANGED --------> success"),
                (equipment, dcError) -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND SPEED DISPLAY FROM SPEED CHANGED --------> ERROR"));
        notifyManager();
    }

    @Override
    public void onCurrentSessionCumulativeKCalChanged(int i) {
        if (computationStrategy == null) {
            computeKcal();
        }
        if (equipmentVersion > EQUIPMENT_FIRMWARE_1_5
                || equipmentID != null && equipmentVersion >= BluetoothEquipmentConsoleUtils.consoleNewDisplayIdVersionMap.get(
                equipmentID)
                || computationStrategy == null && equipmentID != null) {
            if (lastKcalObtainedFromEquipment == 999f && i == 0) {
                lastKnownCumulativeKcal += 1000f;
            }
            bluetoothSportStats.setKcalPerHour(i + lastKnownCumulativeKcal);
            EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_CALORIE, bluetoothSportStats));

        }
        lastKcalObtainedFromEquipment = i;
        if (started) {
            BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                    equipmentID, equipmentVersion, KCAL_BURNT, started, bluetoothSportStats.getKcalPerHour(), equipment -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND CALORIES DISPLAY FROM CALORIES CHANGED --------> success"), (equipment, dcError) -> LogUtils.d(
                            "BLUETOOTH MANAGER SEND CALORIES DISPLAY FROM CALORIES CHANGED --------> ERROR"));
        }
        notifyManager();
    }

    @Override
    public void onCurrentSessionCumulativeKMChanged(float v) {
        bluetoothSportStats.setCurrentSessionCumulativeKM(v + lastKnownCumulativeDistance);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_DIS, bluetoothSportStats));

        BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                equipmentID, equipmentVersion, DISTANCE, started,
                bluetoothSportStats.getCurrentSessionCumulativeKM(), equipment -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND DISTANCE DISPLAY FROM DISTANCE CHANGED --------> success"),
                (equipment, dcError) -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND DISTANCE DISPLAY FROM DISTANCE CHANGED --------> ERROR"));
        notifyManager();
    }

    @Override
    public void onAnalogHeartRateChanged(int i) {
        bluetoothSportStats.setAnalogHeartRate(i);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_HEART_RATE, bluetoothSportStats));

        BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                equipmentID, equipmentVersion, CURRENT_HEART_RATE, started, i, equipment -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND HEART RATE DISPLAY FROM HEART RATE CHANGED --------> success"),
                (equipment, dcError) -> LogUtils.d(
                        "BLUETOOTH MANAGER SEND HEART RATE DISPLAY FROM HEART RATE CHANGED --------> ERROR"));
        notifyManager();
    }

    @Override
    public void onCurrentSessionAverageSpeedChanged(float v) {
        bluetoothSportStats.setSessionAverageSpeedChanged(v);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_AVG_SPEED, bluetoothSportStats));

        notifyManager();
    }

    @Override
    public void onCountChanged(int i) {

        notifyManager();
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public DCEquipment getEquipment() {
        return bike;
    }

    @Override
    public boolean isStarted() {
        return bike.getHotKeyStatus() == DCEquipment.DCHotKeyStart;
    }

    @Override
    public boolean isOnTab() {
        return bike.getTabOnEquipment();
    }

    @Override
    public void clearSessionData() {
        setResistance(RESISTANCE_MIN);
        computeKcal();
        lastKnownCumulativeKcal = 0f;
        lastKnownCumulativeDistance = 0f;
        lastKcalObtainedFromEquipment = 0f;
        bluetoothSportStats.setKcalPerHour(0);
        bluetoothSportStats.setAnalogHeartRate(0);
        bluetoothSportStats.setCurrentSessionCumulativeKM(0);
        bluetoothSportStats.setSessionAverageSpeedChanged(0);
        bluetoothSportStats.setSpeedKmPerHour(bike.getSportData().getCurrentSpeedKmPerHour());
        if (isConnected(bike)) {
            bike.setSessionData(DCEquipment.DCSessionDataTypeClear, sessionStopCompletionBlock,
                    (dcEquipment, dcError) -> LogUtils.d(
                            "BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ASKED --------> ERROR"));
        } else {
            LogUtils.d("BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ASKED --------> NOT CONNECTED");
        }
    }

    @Override
    public void setResistance(float value) {
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

    @Override
    public void setSpeedCmd(float value) {
        //do nothing
    }


    @Override
    public EquipmentInfo getEquipmentInfo() {
        return equipmentInfo;
    }

    @Override
    public void clearSportDataListeners() {
        if (bike != null) {
            bike.setListener(null);
            bike.getSportData().setListener(null);
        }
    }

    @Override
    public void pauseClicked(PauseCauseEnum pauseCauseEnum) {
        pause(pauseCauseEnum);
    }

    /**
     * Function used to switch between pause and start equipment
     */
    private void pause(PauseCauseEnum pauseReason) {
        if (started
                && bike.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected
                && (simulatedPause
                || SESSION_START == pauseReason)
            //    && bike.getTabOnEquipment()
                && pauseReason != TAB_NOT_DETECTED) {
            setHotKey(DCEquipment.DCHotKeyStart);
            bike.setFanSpeedLevel(fanSpeed, genericCompletionBlock, genericErrorBlock);
            simulatedPause = false;
            mListener.onEquipmentPause(new EquipmentPauseState(false, pauseReason));
            equipmentDataMonitor.resumePause();
        } else {
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

    @Override
    public void notifyRpmPause() {
        if (!simulatedPause) {
            pause(EQUIPMENT_INACTIVITY);
        }
    }

    @Override
    public void notifyRpmStart() {
        if (simulatedPause) {
            pause(EQUIPMENT_ACTIVITY);
        }
    }

    @Override
    public float getMonitoredBikeRPM() {
        return currentRPM;
    }
}
