package com.sh.ec.bluetooth.manager.treadmill;

import android.util.Log;

import com.appdevice.domyos.DCCompletionBlock;
import com.appdevice.domyos.DCCompletionBlockWithError;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentInfo;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.DCTreadmill;
import com.appdevice.domyos.parameters.treadmill.DCTreadmillWorkoutModeSetInfoParameters;
import com.sh.ec.AppContext;
import com.sh.ec.R;
import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.bluetooth.common.EquipmentPauseState;
import com.sh.ec.bluetooth.manager.BluetoothEquipmentSpecificManager;
import com.sh.ec.bluetooth.manager.ManagerEventListener;
import com.sh.ec.bluetooth.manager.computations.ComputeKCALStrategy;
import com.sh.ec.bluetooth.manager.computations.ComputeKCALStrategyTreadmill;
import com.sh.ec.bluetooth.manager.utils.BluetoothEquipmentConsoleUtils;
import com.sh.ec.bluetooth.manager.utils.DCUnit;
import com.sh.ec.bluetooth.manager.utils.TypeConstants;
import com.sh.ec.entity.EquipmentInfo;
import com.sh.ec.entity.PauseCauseEnum;
import com.sh.ec.event.SportDataChangeEvent;
import com.sh.ec.service.SportDataService;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static com.sh.ec.bluetooth.manager.BluetoothManager.EQUIPMENT_FIRMWARE_1_5;
import static com.sh.ec.bluetooth.manager.BluetoothManager.EQUIPMENT_WORKOUT_ERROR_CODE;
import static com.sh.ec.bluetooth.manager.BluetoothManager.isConnected;
import static com.sh.ec.entity.PauseCauseEnum.MOTOR_KEY_DETECTED;
import static com.sh.ec.entity.PauseCauseEnum.SESSION_START;
import static com.sh.ec.entity.PauseCauseEnum.SESSION_STOP;
import static com.sh.ec.entity.PauseCauseEnum.TAB_DETECTED;
import static com.sh.ec.entity.PauseCauseEnum.TAB_NOT_DETECTED;
import static com.sh.ec.entity.PauseCauseEnum.USER_REQUEST_PAUSE;
import static com.sh.ec.entity.PauseCauseEnum.USER_REQUEST_START;

/**
 * Class used to manage interactions with treadmill
 * <p>
 * Created by mbouchagour on 12/05/2017.
 */
public class BluetoothTreadmillManagerDemo
        implements TreadmillListener, BluetoothEquipmentSpecificManager {
    public static final int MAX_SLOPE_VALUE = 22;

    public static final int[] console1IdList =
            AppContext.getInstance().getResources().getIntArray(R.array.treadmillConsole1IdList);
    public static final int[] console2IdList =
            AppContext.getInstance().getResources().getIntArray(R.array.treadmillConsole2IdList);
    public static final int[] console3IdList =
            AppContext.getInstance().getResources().getIntArray(R.array.treadmillConsole3IdList);
    public static final int[] console4IdList =
            AppContext.getInstance().getResources().getIntArray(R.array.treadmillConsole4IdList);
    public static final int[] consoleUnknownIdList =
            AppContext.getInstance().getResources().getIntArray(R.array.treadmillUnknownIdList);

    private static final int[] consoleSpecificZoneIdList =
            AppContext.getInstance().getResources().getIntArray(R.array.treadmillSpecificZoneIdList);
    private static final int[] consoleComputedIdList = AppContext.getInstance()
            .getResources()
            .getIntArray(R.array.treadmillComputedCalorieIdList);

    private static final int DEFAULT_EQUIPMENT_INFO_MAX_VALUE = 15;
    private static final int DEFAULT_EQUIPMENT_INFO_MIN_VALUE = 0;
    private static final int DEFAULT_WORKOUT_RESEND_DELAY = 2000;

    private static final float DEFAULT_SPEED_STEP = 0.5f;
    private static final float DEFAULT_INCLINE_STEP = 0.5f;

    private float DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE = 0.5f;
    private int DEFAULT_EQUIPMENT_SAFE_UNKNOWN_ID_REPLACEMENT = 5400001;

    private Boolean isMetric = true;

    private DCTreadmill treadmill;
    private final EquipmentInfo equipmentInfo = new EquipmentInfo();
    public static BluetoothSportStats bluetoothSportStats = new BluetoothSportStats();
    private ManagerEventListener mListener;
    private float weight = 0;
    private float equipmentVersion = -1;
    private Integer equipmentID;

    private int speedMax = DEFAULT_EQUIPMENT_INFO_MAX_VALUE;
    private int inclineMax = DEFAULT_EQUIPMENT_INFO_MAX_VALUE;

    private float inclineObjective = 0f;
    private float previousIncline = 0f;
    private float speedObjective = 0.5f;

    private float lastKnownCumulativeDistance = 0f;
    private float lastKnownCumulativeKcal = 0f;
    private float lastKcalObtainedFromEquipment = 0f;

    private boolean started = false;
    private boolean isSafetyEnabled = true;
    private boolean clearData = false;

    private int[] tableSpeedValueBt;
    private int[] tableInclineValueBt;

    private ComputeKCALStrategy computationStrategy = new ComputeKCALStrategyTreadmill();

    private final DCTreadmillWorkoutModeSetInfoParameters infoParams =
            new DCTreadmillWorkoutModeSetInfoParameters();

    private final DCCompletionBlockWithError genericErrorBlock = new DCCompletionBlockWithError() {
        @Override
        public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
            Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
                    dcEquipment.getName(), dcError.getDescription());
            if (dcError.getCode() == EQUIPMENT_WORKOUT_ERROR_CODE) {
                //avoid to spam console by delay calls
                Timber.i("BLUETOOTH MANAGER DOMYOS WORKOUT ERROR OCCURRED, TRYING DELAYED CALL ...");
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isConnected(treadmill)) {
                            //Switch equipment to workout mode for practice
                            if (treadmill.getMode() != DCEquipment.DCEquipmentModeWorkout) {
                                Timber.i("... BLUETOOTH MANAGER DOMYOS WORKOUT DELAYED CALL");
                                treadmill.setMode(DCEquipment.DCEquipmentModeWorkout,
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
        @Override
        public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
            Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
                    dcEquipment.getName(), dcError.getDescription());
            if (dcEquipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
                Timber.i("BLUETOOTH MANAGER DOMYOS ID ERROR OCCURRED, TRYING DELAYED CALL ...");
                //avoid to spam console by delay calls
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (isConnected(treadmill)) {
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

    /**
     * Used to trigger equipment pause after session stop and data cleared
     */
    private final DCCompletionBlock sessionStopCompletionBlock = dcEquipment -> {
        Timber.i("BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ------> SUCCESS... ASKING SESSION PAUSE...");
        pauseClicked(SESSION_STOP);
    };
    private boolean isPauseRequested;

    public BluetoothTreadmillManagerDemo(final ManagerEventListener mListener, DCTreadmill treadmill,
                                         float weight, Boolean isMetric) {
        this.treadmill = treadmill;
        this.mListener = mListener;
        this.isMetric = isMetric;
        initialize(treadmill, false);
        this.weight = weight;
    }

    public void initialize(DCTreadmill connectedEquipment, final boolean clearData) {
        Timber.i("BLUETOOTH MANAGER DOMYOS INITIALIZE EQUIPMENT");
        this.clearData = clearData;
        if (!clearData) {
            lastKnownCumulativeDistance = bluetoothSportStats.getCurrentSessionCumulativeKM();
            lastKnownCumulativeKcal = bluetoothSportStats.getKcalPerHour();
            lastKcalObtainedFromEquipment = 0;
        }
        //Switch equipment to workout mode for practice
        treadmill = connectedEquipment;
        //Set equipment mode to workout to prepare practice
        Timber.i(
                "BLUETOOTH MANAGER DOMYOS -----------------------TREADMILL BEGIN--------------------");
        if (treadmill.getMode() != DCEquipment.DCEquipmentModeWorkout) {
            Timber.i("BLUETOOTH MANAGER DOMYOS ASK WORKOUT...");
            treadmill.setMode(DCEquipment.DCEquipmentModeWorkout,
                    dcEquipment -> onWorkoutCompletion(clearData), genericErrorBlock);
        } else {
            Timber.i("BLUETOOTH MANAGER DOMYOS ALREADY WORKOUT");
            onWorkoutCompletion(clearData);
        }
        //listen to console and practice callbacks
        Timber.i("BLUETOOTH MANAGER DOMYOS REGISTER EQUIPMENT EVENT LISTENER");
    }

    /**
     * Interact with equipment only in workout mode
     */
    private void onWorkoutCompletion(final boolean clearData) {
        Timber.i("BLUETOOTH MANAGER DOMYOS IN WORKOUT MODE !");
        equipmentInfo.setMaxSpeed(speedMax);
        equipmentInfo.setMaxInclinePercentage(inclineMax);
        equipmentInfo.setMinInclinePercentage(DEFAULT_EQUIPMENT_INFO_MIN_VALUE);
        Timber.i("BLUETOOTH MANAGER DOMYOS ASK EQUIPMENT ID");
        if (isConnected(treadmill)) {

            treadmill.getEquipmentID((dcEquipment, s) -> onIdReceived(s, clearData), idErrorBlock);
            equipmentID = SportDataService.equipmentId;
            equipmentVersion = SportDataService.equipmentVersion;
        }
    }

    private void onIdReceived(String s, boolean clearData) {
        Timber.i("BLUETOOTH MANAGER DOMYOS EQUIPMENT ID RECEIVED : %s", s);
        try {
            equipmentID = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            equipmentID = DEFAULT_EQUIPMENT_SAFE_UNKNOWN_ID_REPLACEMENT;
            Timber.e(e);
        }

        //Get equipment version number
        Timber.i("BLUETOOTH MANAGER DOMYOS ASK EQUIPMENT INFO ...");
        if (isConnected(treadmill)) {
            treadmill.getEquipmentInfo((dcTreadmill, dcEquipmentInfo, dcEquipmentInfo1) -> onEquipmentInfoReceived(s,
                    dcEquipmentInfo, clearData), (dcEquipment, dcError) -> Timber.i(
                    "BLUETOOTH MANAGER DOMYOS ASK EQUIPMENT INFO ...--------> ERROR"));
        }
    }

    private void onEquipmentInfoReceived(String s, DCEquipmentInfo dcEquipmentInfo, boolean clearData) {
        Timber.i("BLUETOOTH MANAGER DOMYOS EQUIPMENT INFO RECEIVED");

        //Get the equipment id and match the corresponding values on console
        if (equipmentID == 84202) {
            equipmentID = 8396836;
            mListener.onEquipmentIdReceived(String.valueOf(equipmentID));
        } else {
            mListener.onEquipmentIdReceived(s);
        }

        initializeMaxAndButtonsValues(equipmentID);

        equipmentInfo.setMaxSpeed(speedMax);
        equipmentInfo.setMaxInclinePercentage(inclineMax);
        equipmentInfo.setMinInclinePercentage(DEFAULT_EQUIPMENT_INFO_MIN_VALUE);
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

        treadmill.setListener(this);
        treadmill.getSportData().setListener(this);
        if (clearData) {
            //clear session data
            Timber.i("BLUETOOTH MANAGER DOMYOS ASK CLEAR SESSION DATA ...");
            if (isConnected(treadmill)) {
                treadmill.setSessionData(DCEquipment.DCSessionDataTypeClear, dcEquipment1 -> Timber.i("BLUETOOTH MANAGER DOMYOS SESSION DATA CLEARED !"),
                        (dcEquipment, dcError) -> Timber.i(
                                "BLUETOOTH MANAGER DOMYOS SESSION DATA CLEAR --------> ERROR"));
            }
            initDisplay();
        } else {
            Timber.i("BLUETOOTH MANAGER RECOVERY OF SESSION ASKING START");
            isSafetyEnabled = treadmill.getSafetyMotorKey();
            pauseClicked(SESSION_START);
            speedObjective = bluetoothSportStats.getSpeedKmPerHour();
            inclineObjective = bluetoothSportStats.getInclinePercentage();
            initDisplay();
            setSpeedCmd(speedObjective);
            setResistance(inclineObjective);
        }
        Timber.i("BLUETOOTH MANAGER DOMYOS END TREADMILL INITIALIZATION");
    }

    /**
     * Match the console ids with their corresponding max and min values and initialize
     * <p>
     * the matching buttons available for speed and incline change according to the type
     * <p>
     * of console
     *
     * @param equipmentID id of the equipment
     */
    private void initializeMaxAndButtonsValues(Integer equipmentID) {
        if (TypeConstants.contain(console2IdList, equipmentID)) {
            tableSpeedValueBt = new int[]{5, 10, 16, 22};
            tableInclineValueBt = new int[]{0, 5, 10, 15};

            speedMax = 22;
            inclineMax = 15;
        } else if (TypeConstants.contain(console1IdList, equipmentID)) {
            tableSpeedValueBt = new int[]{5, 10, 16, 18};
            tableInclineValueBt = new int[]{0, 5, 10, 20};

            speedMax = 18;
            inclineMax = 20;
        } else if (TypeConstants.contain(console3IdList, equipmentID)) {

            tableSpeedValueBt = new int[]{4, 6, 8, 10, 12, 14, 16, 18};
            tableInclineValueBt = new int[]{0, 1, 2, 3, 4, 6, 8, 10};

            DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE = 1f;
            speedObjective = 1f;

            speedMax = 18;
            inclineMax = 10;
        } else if (TypeConstants.contain(console4IdList, equipmentID)) {

            tableSpeedValueBt = new int[]{3, 5, 8, 10, 13, 16};
            tableInclineValueBt = new int[]{0, 2, 4, 6, 8, 10};

            speedMax = 16;
            inclineMax = 10;
        } else if (TypeConstants.contain(consoleUnknownIdList, equipmentID)) {

            speedMax = 20;
            inclineMax = 18;
        } else {
            inclineMax = 15;
            speedMax = 15;
        }
    }


    /**
     * Initiate program beginning
     */
    public void startProgram() {
        started = true;
        setSpeedCmd(speedObjective);
        setResistance(inclineObjective);
        if (treadmill.getTabOnEquipment()) {
            pauseClicked(SESSION_START);
        } else {
            pauseClicked(TAB_NOT_DETECTED);
        }
      //  initDisplay();
    }

    @Override
    public void stopProgram() {
        started = false;
        BluetoothEquipmentConsoleUtils.displayMainMessage(TypeConstants.TYPE_SPORT_TREADMILL,
                treadmill, equipmentID, equipmentVersion, started, dcEquipment -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM STOP PROGRAM --------> success"),
                (dcEquipment, dcError) -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM STOP PROGRAM --------> ERROR"));
        clearSessionData();
    }

    @Override
    public boolean isProgramStarted() {
        return started;
    }

    private void initDisplay() {
        displayBluetoothIcon();
        BluetoothEquipmentConsoleUtils.initConsoleDisplay(TypeConstants.TYPE_SPORT_TREADMILL,
                treadmill, equipmentID, equipmentVersion, started, dcEquipment -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM INIT DISPLAY 1 --------> success"),
                (dcEquipment, dcError) -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM INIT DISPLAY 1 --------> ERROR"), true, 0, 0);
    }

    /**
     * send hotKey commands to equipments
     */
    private void setHotKey(int hotKey) {
        treadmill.setHotKey(hotKey,
                dcEquipment -> Timber.i("BLUETOOTH MANAGER SET PAUSE/START STATE --------> success"),
                (dcEquipment, dcError) -> Timber.i(
                        "BLUETOOTH MANAGER SET PAUSE/START STATE --------> ERROR"));
    }

    /**
     * Change heart color on treadmill console with the given color
     */
    private void displayHeart(int heartColorIndex) {
        infoParams.setHeartRateLedColor(heartColorIndex);
        sendWorkoutInfoParamsToEquipment(infoParams);
    }

    /**
     * Display the bluetooth icon on the treadmill console
     */
    private void displayBluetoothIcon() {
        infoParams.setBtLedSwitch(true);
        sendWorkoutInfoParamsToEquipment(infoParams);
    }

    /**
     * Change incline percentage and display it on console
     */
    private void setInclinePercentage(float inclinePercentage) {
        infoParams.setTargetInclinePercentage(inclinePercentage);
        sendWorkoutInfoParamsToEquipment(infoParams);
        BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_TREADMILL,
                treadmill, equipmentID, equipmentVersion, DCUnit.SLOPE_DEVICE, started,
                inclinePercentage, dcEquipment -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM SET INCLINE PERCENTAGE --------> success"),
                (dcEquipment, dcError) -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM SET INCLINE PERCENTAGE --------> ERROR"));
    }

    /**
     * Change speed and display it on console
     */
    private void setSpeed(float speed) {
        infoParams.setCurrentSpeedKmPerHour(speed);
        sendWorkoutInfoParamsToEquipment(infoParams);

        BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_TREADMILL,
                treadmill, equipmentID, equipmentVersion, DCUnit.CURRENT_SPEED, started,
                TypeConstants.convertData(TypeConstants.TYPE_DISTANCE,
                        TypeConstants.convertInMeters(speed), isMetric), dcEquipment -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM SET SPEED --------> success"),
                (dcEquipment, dcError) -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM SET SPEED --------> ERROR"));
    }

    /**
     * User interactions with console buttons
     */
    private void handleButtonTap(int buttonIndex, PauseCauseEnum pauseReason) {
        int fanSpeed = treadmill.getFanSpeedLevel();
        float rest = speedObjective % 1;
        switch (buttonIndex) {
            case DCTreadmill.DCTreadmillPressedButtonInclinePlus:
                if (inclineObjective + DEFAULT_INCLINE_STEP <= inclineMax) {
                    inclineObjective += DEFAULT_INCLINE_STEP;
                    setInclinePercentage(inclineObjective);
                }
                break;
            case DCTreadmill.DCTreadmillPressedButtonInclineMinu:
                if (inclineObjective - DEFAULT_INCLINE_STEP >= DEFAULT_EQUIPMENT_INFO_MIN_VALUE) {
                    inclineObjective -= DEFAULT_INCLINE_STEP;
                    setInclinePercentage(inclineObjective);
                }
                break;

            case DCTreadmill.DCTreadmillPressedButtonSpeedMinus:
                if (rest > DEFAULT_SPEED_STEP) {
                    setSpeedCmd((float) (Math.floor(speedObjective) + DEFAULT_SPEED_STEP));
                } else if (rest > 0 && rest < DEFAULT_SPEED_STEP) {
                    setSpeedCmd((float) Math.floor(speedObjective));
                } else {
                    speedObjective -= DEFAULT_SPEED_STEP;
                    setSpeedCmd(speedObjective);
                }
                break;
            case DCTreadmill.DCTreadmillPressedButtonSpeedPlus:
                if (rest > DEFAULT_SPEED_STEP) {
                    setSpeedCmd((float) (Math.ceil(speedObjective)));
                } else if (rest > 0 && rest < DEFAULT_SPEED_STEP) {
                    setSpeedCmd((float) (Math.floor(speedObjective) + DEFAULT_SPEED_STEP));
                } else {
                    speedObjective += DEFAULT_SPEED_STEP;
                    setSpeedCmd(speedObjective);
                }
                break;
            case DCTreadmill.DCTreadmillPressedButtonFanMinus:
                if (fanSpeed > 0 && isConnected(treadmill)) {
                    treadmill.setFanSpeedLevel(fanSpeed - 1, dcEquipment -> Timber.i(
                            "BLUETOOTH MANAGER SET FAN SPEED 1 --------> success"),
                            (dcEquipment, dcError) -> Timber.i(
                                    "BLUETOOTH MANAGER SET FAN SPEED 1 --------> ERROR"));
                }
                break;
            case DCTreadmill.DCTreadmillPressedButtonFanPlus:
                if (fanSpeed < 5 && isConnected(treadmill)) {
                    treadmill.setFanSpeedLevel(fanSpeed + 1, dcEquipment -> Timber.i(
                            "BLUETOOTH MANAGER SET FAN SPEED 2 --------> success"),
                            (dcEquipment, dcError) -> Timber.i(
                                    "BLUETOOTH MANAGER SET FAN SPEED 2 --------> ERROR"));
                }
                break;
            case DCTreadmill.DCTreadmillPressedButtonStop:
                pauseClicked(SESSION_STOP);
                break;
            case DCTreadmill.DCTreadmillPressedButtonStartPause:
                if (treadmill.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
                    if (pauseReason == null) {
                        pauseReason = USER_REQUEST_PAUSE;
                    }
                    if (started
                            && (treadmill.getHotKeyStatus() == DCEquipment.DCHotKeyPause || pauseReason == SESSION_START)
                            && treadmill.getTabOnEquipment()
                            && treadmill.getSafetyMotorKey()
                            && isSafetyEnabled
                            && pauseReason != SESSION_STOP) {
                        isPauseRequested = false;
                        setHotKey(DCEquipment.DCHotKeyStart);
                        setSpeedCmd(speedObjective);

                        //old console send wrong incline value before knowing motorkey changed so
                        if (pauseReason == MOTOR_KEY_DETECTED && (TypeConstants.contain(
                                console1IdList, equipmentID) || TypeConstants.contain(console2IdList,
                                equipmentID))) {
                            inclineObjective = previousIncline;
                            setInclinePercentage(previousIncline);
                        } else {
                            setInclinePercentage(inclineObjective);
                        }
                        mListener.onEquipmentPause(new EquipmentPauseState(false, pauseReason));
                    } else {
                        if (pauseReason != USER_REQUEST_START) {
                            isPauseRequested = true;
                            setHotKey(DCEquipment.DCHotKeyPause);
                            mListener.onEquipmentPause(new EquipmentPauseState(true, pauseReason));
                        }
                    }
                }
                break;
            case DCTreadmill.DCTreadmillPressedButtonIncline1:
                setInclineFromTable(0);
                break;
            case DCTreadmill.DCTreadmillPressedButtonIncline2:
                setInclineFromTable(1);
                break;
            case DCTreadmill.DCTreadmillPressedButtonIncline3:
                setInclineFromTable(2);
                break;
            case DCTreadmill.DCTreadmillPressedButtonIncline4:
                setInclineFromTable(3);
                break;
            case DCTreadmill.DCTreadmillPressedButtonIncline5:
                setInclineFromTable(4);
                break;
            case DCTreadmill.DCTreadmillPressedButtonIncline6:
                setInclineFromTable(5);
                break;
            case DCTreadmill.DCTreadmillPressedButtonIncline7:
                setInclineFromTable(6);
                break;
            case DCTreadmill.DCTreadmillPressedButtonIncline8:
                setInclineFromTable(7);
                break;

            case DCTreadmill.DCTreadmillPressedButtonSpeed1:
                setSpeedFromTable(0);
                break;
            case DCTreadmill.DCTreadmillPressedButtonSpeed2:
                setSpeedFromTable(1);
                break;
            case DCTreadmill.DCTreadmillPressedButtonSpeed3:
                setSpeedFromTable(2);
                break;
            case DCTreadmill.DCTreadmillPressedButtonSpeed4:
                setSpeedFromTable(3);
                break;
            case DCTreadmill.DCTreadmillPressedButtonSpeed5:
                setSpeedFromTable(4);
                break;
            case DCTreadmill.DCTreadmillPressedButtonSpeed6:
                setSpeedFromTable(5);
                break;
            case DCTreadmill.DCTreadmillPressedButtonSpeed7:
                setSpeedFromTable(6);
                break;
            case DCTreadmill.DCTreadmillPressedButtonSpeed8:
                setSpeedFromTable(7);
                break;
            default:
                break;
        }
    }

    /**
     * Send the new speed value to the equipment matching the corresponding button according to
     * predefined values
     */
    private void setSpeedFromTable(int tableIndex) {
        if (tableSpeedValueBt != null && tableSpeedValueBt.length > tableIndex) {
            speedObjective = tableSpeedValueBt[tableIndex];
            setSpeedCmd(speedObjective);
        }
    }

    /**
     * Send the new incline value to the equipment according to predefined values
     */
    private void setInclineFromTable(int tableIndex) {
        if (tableInclineValueBt != null && tableInclineValueBt.length > tableIndex) {
            inclineObjective = tableInclineValueBt[tableIndex];
            setInclinePercentage(inclineObjective);
        }
    }

    /**
     * Send values to the treadmill console
     */
    private void sendWorkoutInfoParamsToEquipment(
            DCTreadmillWorkoutModeSetInfoParameters infoParams) {
        if (isConnected(treadmill)) {
            treadmill.setWorkoutModeInfoValue(infoParams, dcEquipment -> Timber.i(
                    "BLUETOOTH MANAGER SEND INFO PARAMS (speed, incline, bluetooth icon ...) --------> success"),
                    (dcEquipment, dcError) -> Timber.i(
                            "BLUETOOTH MANAGER SEND INFO PARAMS (speed, incline, bluetooth icon ...) --------> ERROR"));
        }
    }

    @Override
    public void notifyManager() {
        if (mListener != null) {
            if (equipmentVersion <= EQUIPMENT_FIRMWARE_1_5
                    || equipmentID != null && (TypeConstants.contain(consoleComputedIdList, equipmentID))) {

                bluetoothSportStats.setKcalPerHour((float) (bluetoothSportStats.getKcalPerHour()
                        + (computationStrategy.computeKCALValue(this) / 3600f)));
            }

            if (equipmentID != null && TypeConstants.contain(consoleSpecificZoneIdList, equipmentID)) {

                BluetoothEquipmentConsoleUtils.displayZoneInformation(
                        TypeConstants.TYPE_SPORT_TREADMILL, treadmill, equipmentID, equipmentVersion,
                        DCUnit.KCAL_BURNT, started, bluetoothSportStats.getKcalPerHour(),
                        dcEquipment -> Timber.i(
                                "BLUETOOTH MANAGER SEND DISPLAY FROM COMPUTED KCAL 1 --------> success"),
                        (dcEquipment, dcError) -> Timber.i(
                                "BLUETOOTH MANAGER SEND DISPLAY FROM COMPUTED KCAL 1 --------> ERROR"));

                BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_TREADMILL,
                        treadmill, equipmentID, equipmentVersion, DCUnit.DISTANCE, started,
                        TypeConstants.convertData(TypeConstants.TYPE_DISTANCE,
                                TypeConstants.convertInMeters(
                                        bluetoothSportStats.getCurrentSessionCumulativeKM()), isMetric),
                        dcEquipment -> Timber.i(
                                "BLUETOOTH MANAGER SEND DISPLAY FROM COMPUTED KCAL 2 --------> success"),
                        (dcEquipment, dcError) -> Timber.i(
                                "BLUETOOTH MANAGER SEND DISPLAY FROM COMPUTED KCAL 2 --------> ERROR"));
            }
            mListener.onSportDataReceived(bluetoothSportStats);
        }
    }

    //console interactions section
    @Override
    public void treadmillOnSafetyMotorKeyChanged(DCEquipment dcEquipment, boolean b) {
       Log.e("Manger","treadmillOnSafetyMotorKeyChanged --------> success=="+b);

        if (treadmill.getSafetyMotorKey()) {
            if (!isStarted()) {
                isSafetyEnabled = true;
                mListener.onEquipmentPause(new EquipmentPauseState(true, MOTOR_KEY_DETECTED));
            }
        } else {
            isSafetyEnabled = false;
            pauseClicked(PauseCauseEnum.MOTOR_KEY_NOT_DETECTED);
        }
    }

    @Override
    public void equipmentTabOnEquipmentChanged(DCEquipment dcEquipment, boolean b) {
        Log.e("Manger","equipmentTabOnEquipmentChanged --------> success=="+b);

        if (treadmill.getTabOnEquipment()) {
            BluetoothEquipmentConsoleUtils.displayZoneInformation(
                    TypeConstants.TYPE_SPORT_TREADMILL, treadmill, equipmentID, equipmentVersion,
                    DCUnit.CURRENT_HEART_RATE, started, 0, equipment -> Timber.i(
                            "BLUETOOTH MANAGER SEND DISPLAY FROM TAB CHANGED --------> success"),
                    (equipment, dcError) -> Timber.i(
                            "BLUETOOTH MANAGER SEND DISPLAY FROM TAB CHANGED --------> ERROR"));
        }

        if (!treadmill.getTabOnEquipment()) {
            handleButtonTap(DCTreadmill.DCTreadmillPressedButtonStartPause, TAB_NOT_DETECTED);
        }

        if (treadmill.getTabOnEquipment()) {
            handleButtonTap(DCTreadmill.DCTreadmillPressedButtonStartPause, TAB_DETECTED);
        }

        if (mListener != null) {
            mListener.onEquipmentTabChange(treadmill.getTabOnEquipment());
        }
    }

    @Override
    public void equipmentErrorOccurred(DCEquipment dcEquipment, int i) {
        Log.e("Manger","equipmentErrorOccurred --------> success=="+String.valueOf(i));

        Timber.i("BLUETOOTH MANAGER TREADMILL ERROR OCCURRED -----> ERROR CODE = %s",
                String.valueOf(i));
        if (i != 200 && i != 255 && i != 10 && i != 0) {
            Timber.i(
                    "BLUETOOTH MANAGER TREADMILL ERROR OCCURRED -----> CONSOLE REBOOT COMMAND HAS BEEN SENT");
        }
    }

    @Override
    public void equipmentPressedButtonChanged(DCEquipment dcEquipment, int i) {
        Log.e("Manger","equipmentPressedButtonChanged --------> success=="+i);

        handleButtonTap(i, null);
    }

    @Override
    public void equipmentOnHotKeyStatusChanged(DCEquipment dcEquipment, int i) {
        Log.e("Manger","equipmentOnHotKeyStatusChanged --------> success=="+i);

        //not used
        handleButtonTap(i, null);
    }

    @Override
    public void equipmentOnFanSpeedLevelChanged(DCEquipment dcEquipment, int i) {
        Log.e("Manger","equipmentOnFanSpeedLevelChanged --------> success=="+i);
        handleButtonTap(i, null);
    }

    //sport data section
    @Override
    public void onTargetInclinePercentageChanged(float v) {
        bluetoothSportStats.setInclinePercentage(v);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_INCLINE,bluetoothSportStats));
        Log.e("Manger","onTargetInclinePercentageChanged --------> success=="+v);
        if (!isPauseRequested) {
            previousIncline = inclineObjective;
            inclineObjective = v;
            BluetoothEquipmentConsoleUtils.displayZoneInformation(
                    TypeConstants.TYPE_SPORT_TREADMILL, treadmill, equipmentID, equipmentVersion,
                    DCUnit.SLOPE_DEVICE, started, v, dcEquipment -> Timber.i(
                            "BLUETOOTH MANAGER SEND DISPLAY FROM INCLINE CHANGED --------> success"),
                    (dcEquipment, dcError) -> Timber.i(
                            "BLUETOOTH MANAGER SEND DISPLAY FROM INCLINE CHANGED --------> ERROR"));
            bluetoothSportStats.setInclinePercentage(v);
            notifyManager();
        }
    }

    @Override
    public void onCurrentSpeedKmPerHourChanged(float v) {
        Log.e("Manger","onCurrentSpeedKmPerHourChanged --------> success=="+v);
        bluetoothSportStats.setSpeedKmPerHour(v);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_SPEED,bluetoothSportStats));

        if (!isPauseRequested) {
            speedObjective = v;
            BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_TREADMILL,
                    treadmill, equipmentID, equipmentVersion, DCUnit.CURRENT_SPEED, started,
                    TypeConstants.convertData(TypeConstants.TYPE_DISTANCE,
                            TypeConstants.convertInMeters(v), isMetric), dcEquipment -> Timber.i(
                            "BLUETOOTH MANAGER SEND DISPLAY FROM SPEED CHANGED --------> success"),
                    (dcEquipment, dcError) -> Timber.i(
                            "BLUETOOTH MANAGER SEND DISPLAY FROM SPEED CHANGED --------> ERROR"));
            bluetoothSportStats.setSpeedKmPerHour(v);
            notifyManager();


        }
    }

    @Override
    public void onCurrentSessionCumulativeKCalChanged(int i) {
        Log.e("Manger","onCurrentSessionCumulativeKCalChanged --------> success=="+i);

        if ((equipmentVersion > EQUIPMENT_FIRMWARE_1_5
                || equipmentID != null
                && equipmentVersion >= BluetoothEquipmentConsoleUtils.consoleNewDisplayIdVersionMap.get(
                equipmentID)) && equipmentID != null && !(TypeConstants.contain(consoleComputedIdList,
                equipmentID))) {
            if (lastKcalObtainedFromEquipment == 999f && i == 0) {
                lastKnownCumulativeKcal += 1000f;
            }
            bluetoothSportStats.setKcalPerHour(i + lastKnownCumulativeKcal);
        }
        lastKcalObtainedFromEquipment = i;
        bluetoothSportStats.setKcalPerHour(i);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_CALORIE,bluetoothSportStats));
        notifyManager();
    }

    @Override
    public void onCurrentSessionCumulativeKMChanged(float v) {
        Log.e("Manger","onCurrentSessionCumulativeKMChanged --------> success=="+v);

        bluetoothSportStats.setCurrentSessionCumulativeKM(v + lastKnownCumulativeDistance);
      //  bluetoothSportStats.setCurrentSessionCumulativeKM(v);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_DIS,bluetoothSportStats));
        notifyManager();
    }

    @Override
    public void onAnalogHeartRateChanged(int i) {
        Log.e("Manger","onAnalogHeartRateChanged --------> success=="+i);

        bluetoothSportStats.setAnalogHeartRate(i);
        notifyManager();
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_HEART_RATE,bluetoothSportStats));

        BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_TREADMILL,
                treadmill, equipmentID, equipmentVersion, DCUnit.CURRENT_HEART_RATE, started, i,
                dcEquipment -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM HEART RATE CHANGED --------> success"),
                (dcEquipment, dcError) -> Timber.i(
                        "BLUETOOTH MANAGER SEND DISPLAY FROM HEART RATE CHANGED --------> ERROR"));
    }

    @Override
    public void onCurrentSessionAverageSpeedChanged(float v) {
        Log.e("Manger","onCurrentSessionAverageSpeedChanged --------> success=="+v);
       /* Intent intentRunAr = new Intent("com.domyos.econnected.SEND_SPEED");
        Bundle bundleRunAr = new Bundle();
        bundleRunAr.putFloat("avg_speed", v);
        intentRunAr.putExtras(bundleRunAr);
        AppContext.getInstance().sendBroadcast(intentRunAr);*/
        bluetoothSportStats.setSessionAverageSpeedChanged(v);
        EventBus.getDefault().post(new SportDataChangeEvent(SportDataChangeEvent.ACTION_AVG_SPEED,bluetoothSportStats));

        notifyManager();
    }

    @Override
    public void onCountChanged(int i) {

        Log.e("Manger","onCurrentSessionAverageSpeedChanged --------> success=="+i);
        notifyManager();
    }

    @Override
    public BluetoothSportStats getBluetoothSportStats() {
        return bluetoothSportStats;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public DCEquipment getEquipment() {
        return treadmill;
    }

    @Override
    public void pauseClicked(PauseCauseEnum pauseCauseEnum) {
        handleButtonTap(DCTreadmill.DCTreadmillPressedButtonStartPause, pauseCauseEnum);
    }

    @Override
    public boolean isStarted() {
        return treadmill.getHotKeyStatus() == DCEquipment.DCHotKeyStart;
    }

    @Override
    public EquipmentInfo getEquipmentInfo() {
        return equipmentInfo;
    }

    @Override
    public void clearSportDataListeners() {
        if (treadmill != null) {
            treadmill.setListener(null);
            treadmill.getSportData().setListener(null);
        }
    }

    @Override
    public boolean isOnTab() {
        return treadmill.getTabOnEquipment();
    }

    @Override
    public void clearSessionData() {
        speedObjective = DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE;
        inclineObjective = DEFAULT_EQUIPMENT_INFO_MIN_VALUE;
        setSpeedCmd(DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE);
        setResistance(DEFAULT_EQUIPMENT_INFO_MIN_VALUE);
        lastKnownCumulativeKcal = 0f;
        lastKcalObtainedFromEquipment = 0f;
        lastKnownCumulativeDistance = 0f;

        bluetoothSportStats.setKcalPerHour(0);
        bluetoothSportStats.setAnalogHeartRate(0);
        bluetoothSportStats.setCurrentSessionCumulativeKM(0);
        bluetoothSportStats.setSessionAverageSpeedChanged(0);
        bluetoothSportStats.setSpeedKmPerHour(DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE);
        bluetoothSportStats.setInclinePercentage(DEFAULT_EQUIPMENT_INFO_MIN_VALUE);

        if (isConnected(treadmill)) {
            treadmill.setSessionData(DCEquipment.DCSessionDataTypeClear, sessionStopCompletionBlock,
                    (dcEquipment, dcError) -> Timber.i(
                            "BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ASKED --------> ERROR"));
        }
    }

    @Override
    public void setResistance(float value) {
        inclineObjective = value;
        if (inclineObjective > DEFAULT_EQUIPMENT_INFO_MIN_VALUE && inclineObjective < inclineMax) {
            setInclinePercentage(inclineObjective);
        }

        if (inclineObjective <= DEFAULT_EQUIPMENT_INFO_MIN_VALUE) {
            inclineObjective = DEFAULT_EQUIPMENT_INFO_MIN_VALUE;
            setInclinePercentage(DEFAULT_EQUIPMENT_INFO_MIN_VALUE);
        }

        if (inclineObjective >= inclineMax) {
            inclineObjective = inclineMax;
            setInclinePercentage(inclineMax);
        }
    }

    @Override
    public void setSpeedCmd(float value) {
        speedObjective = value;

        if (speedObjective > DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE && speedObjective < speedMax) {
            setSpeed(speedObjective);
        }

        if (speedObjective <= DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE) {
            speedObjective = DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE;
            setSpeed(DEFAULT_EQUIPMENT_INFO_MIN_SPEED_VALUE);
        }

        if (speedObjective >= speedMax) {
            speedObjective = speedMax;
            setSpeed(speedMax);
        }
    }
}
