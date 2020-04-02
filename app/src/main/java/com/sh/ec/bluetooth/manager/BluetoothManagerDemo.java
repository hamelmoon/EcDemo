package com.sh.ec.bluetooth.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.DCTreadmill;
import com.sh.ec.bluetooth.BluetoothConnectionState;
import com.sh.ec.bluetooth.BluetoothEquipmentService;
import com.sh.ec.bluetooth.common.BluetoothDiscoveredEquipment;
import com.sh.ec.bluetooth.common.BluetoothEquipmentConnectionState;
import com.sh.ec.bluetooth.common.BluetoothSportStats;
import com.sh.ec.bluetooth.common.EquipmentPauseState;
import com.sh.ec.bluetooth.manager.connection.BluetoothConnectionManager;
import com.sh.ec.bluetooth.manager.treadmill.BluetoothTreadmillManager;
import com.sh.ec.bluetooth.manager.utils.ErrorUtils;
import com.sh.ec.bluetooth.manager.utils.TypeConstants;
import com.sh.ec.entity.EquipmentInfo;
import com.sh.ec.entity.PauseCauseEnum;
import com.sh.ec.utils.DomyosException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

import static com.sh.ec.entity.PauseCauseEnum.BLUETOOTH_DISCONNECTED;
import static com.sh.ec.entity.PauseCauseEnum.TAB_DETECTED;
import static com.sh.ec.entity.PauseCauseEnum.TAB_NOT_DETECTED;

/**
 * Manager used to communicate with domyos api. The manager use one sub manager for handling
 * connection with the DCEquipmentManager of the Bluetooth SDK.
 * <p>
 * Another sub manager specific to an equipment type handle equipment interactions and is created
 * when connected to an equipment.
 * <p>
 * Created by mbouchagour on 19/04/2017.
 */
public class BluetoothManagerDemo
        implements BluetoothEquipmentService, ManagerEventListener {
    private static final int EQUIPMENT_DISCONNECTED_ERROR_CODE = DCEquipment.DCErrorCodeNotConnected;
    private static final int EQUIPMENT_RETRY_COUNT_ERROR_CODE =
            DCEquipment.DCErrorCodeRetryCountExceeded;
    public static final int EQUIPMENT_WORKOUT_ERROR_CODE = DCEquipment.DCErrorCodeChangeMode;
    private static final long MERGE_SCANNED_EQUIPMENTS_INTERVAL = 500;

    public static final float EQUIPMENT_FIRMWARE_1_5 = 1.5f;
    public static final int DEFAULT_NO_CONSOLE_ID_NUMBER = -1;
/*
  private final Preference<Integer> lastUsedEquimentType;*/

    /**
     * Equipment specific manager set when connected
     */
    private BluetoothEquipmentSpecificManager specificEquipmentManager;

    private String selectedEquipment;
    private String firstEquipmentID;
    private boolean connecteForFirstEquipmentID = false;
    private DCEquipment oldConnectedEquipment;
    public List<BluetoothDiscoveredEquipment> oldScannedEquipmentList = null;
    /**
     * Sub manager handling connection
     */
    private BluetoothConnectionManager connectionManager;

    /**
     * Fields used to regularly notify connected Rx subscriber with the corresponding event
     */
    private final BehaviorSubject<BluetoothEquipmentConnectionState> dcConnectionPublishSubject =
            BehaviorSubject.create();
    private final PublishSubject<Collection<DCEquipment>> dcScanPublishSubject =
            PublishSubject.create();
    private final PublishSubject<BluetoothSportStats> dcPublishSubject = PublishSubject.create();

    private final PublishSubject<EquipmentPauseState> dcPausePublishSubject = PublishSubject.create();

    private final BehaviorSubject<EquipmentPauseState> dcTabPublishSubject = BehaviorSubject.create();

    private final BehaviorSubject<EquipmentInfo> dcEquipmentInfoPublishSubject = BehaviorSubject.create();

    public BluetoothManagerDemo(BluetoothConnectionManager bluetoothConnectionManager) {
        this.connectionManager = bluetoothConnectionManager;
        connectionManager.setListener(this);
    }


    /**
     * Return the publisher to get sport callbacks events
     *
     * @return
     */
    @Override
    public Observable<BluetoothSportStats> getBluetoothSportStats() {


        return dcPublishSubject.map(bluetoothSportStats -> bluetoothSportStats);
    }

    @Override
    public Observable<EquipmentPauseState> listenPauseState(boolean pause) {
        return dcPausePublishSubject.map(isPaused -> isPaused);
    }

    @Override
    public void setSelectedEquipmentName(String equipmentName) {
        if (connectionManager.getBluetoothEquipmentConnectionState() != null
                && connectionManager.getBluetoothEquipmentConnectionState().getConnectedEquipment() != null) {
            this.selectedEquipment = connectionManager.getBluetoothEquipmentConnectionState().getConnectedEquipment().getName();
        } else {
            this.selectedEquipment = equipmentName;
        }
        dcScanPublishSubject.onNext(connectionManager.getEquipments());
    }

    @Override
    public int getSelectedIdConsole() {
        try {
            return Integer.valueOf(firstEquipmentID);
        } catch (NumberFormatException e) {
            return DEFAULT_NO_CONSOLE_ID_NUMBER;
        }
    }

    @Override
    public Observable<EquipmentPauseState> listenTabState() {
        //notify of the current done of the connected equipment
        if (specificEquipmentManager != null && specificEquipmentManager.isOnTab()) {
            dcTabPublishSubject.onNext(new EquipmentPauseState(
                    specificEquipmentManager != null && specificEquipmentManager.isOnTab(), TAB_DETECTED));
        } else {
            dcTabPublishSubject.onNext(new EquipmentPauseState(
                    specificEquipmentManager != null && specificEquipmentManager.isOnTab(), TAB_NOT_DETECTED));
        }

        return dcTabPublishSubject.map(isPaused -> isPaused);
    }

    @Override
    public Observable<EquipmentPauseState> pause() {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.pauseClicked(PauseCauseEnum.USER_REQUEST_PAUSE);
        } else {
            errorHandling(EQUIPMENT_DISCONNECTED_ERROR_CODE);
        }
        return Observable.empty();
    }

    @Override
    public Observable<EquipmentPauseState> start() {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.pauseClicked(PauseCauseEnum.USER_REQUEST_START);
        } else {
            errorHandling(EQUIPMENT_DISCONNECTED_ERROR_CODE);
        }
        return Observable.empty();
    }

    public void startSport() {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.startProgram();
        } else {
            errorHandling(EQUIPMENT_DISCONNECTED_ERROR_CODE);
        }

    }

    @Override
    public Observable<Boolean> setResistance(float value) {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.setResistance(value);
        } else {
            errorHandling(EQUIPMENT_DISCONNECTED_ERROR_CODE);
        }
        return Observable.empty();
    }

    @Override
    public Observable<Boolean> setSpeedCmd(float value) {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.setSpeedCmd(value);
        } else {
            errorHandling(EQUIPMENT_DISCONNECTED_ERROR_CODE);
        }
        return Observable.empty();
    }

    @Override
    public Observable<EquipmentInfo> getEquipmentInfo() {
        if (specificEquipmentManager != null) {
            EquipmentInfo mEquipmentInfo = specificEquipmentManager.getEquipmentInfo();

            dcEquipmentInfoPublishSubject.onNext(mEquipmentInfo);
            return dcEquipmentInfoPublishSubject.map(
                    equipmentInfo -> specificEquipmentManager.getEquipmentInfo());
        } else {
            return Observable.empty();
        }
    }

    /**
     * Emits regularly the list of equipment scanned if a change has occurred
     *
     * @return
     */
    @Override
    public Observable<List<BluetoothDiscoveredEquipment>> listenScan() {
        return dcScanPublishSubject.buffer(
                () -> Observable.interval(MERGE_SCANNED_EQUIPMENTS_INTERVAL, TimeUnit.MILLISECONDS))
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable);
                    return Observable.just(new ArrayList<>());
                })
                .map(dcEquipments -> connectionManager.getEquipments())
                .map(this::transformEquipmentList)
                .filter(bluetoothDiscoveredEquipments -> {
                    //we determine if a change has occurred to avoid spamming display
                    if (oldScannedEquipmentList != null) {
                        if (oldScannedEquipmentList.size() != bluetoothDiscoveredEquipments.size()) {
                            oldScannedEquipmentList = bluetoothDiscoveredEquipments;
                            return true;
                        }
                        for (int i = 0; i < oldScannedEquipmentList.size(); i++) {
                            if (!oldScannedEquipmentList.get(i)
                                    .getEquipmentName()
                                    .equals(bluetoothDiscoveredEquipments.get(i).getEquipmentName())) {
                                oldScannedEquipmentList = bluetoothDiscoveredEquipments;
                                return true;
                            }
                        }
                        return false;
                    }
                    oldScannedEquipmentList = bluetoothDiscoveredEquipments;
                    return true;
                });
    }

    @Override
    public Observable<List<BluetoothDiscoveredEquipment>> getEquipmentList() {
        return Observable.just(transformEquipmentList(connectionManager.getEquipments()));
    }

    @Override
    public Observable<String> findEquipmentById(int sportID) {
        for (DCEquipment equipment : connectionManager.getEquipments()) {
            if (getEquipmentType(equipment) == sportID) {
                return Observable.just(equipment.getName());
            }
        }
        return Observable.just("");
    }

    @Override
    public void updateEquipmentList() {
        startScan();
    }

    @Override
    public Observable<String> getEquipmentId() {
        return Observable.just(String.valueOf(getSelectedIdConsole()));
    }

    /**
     * change current connection done and handle actions to do accordingly
     *
     * @return
     */
    @Override
    public Single<Void> setState(final BluetoothConnectionState state) {
        return Single.fromObservable(new Observable<Void>() {
            @Override
            protected void subscribeActual(Observer<? super Void> observer) {
                switch (state) {
                    case NOT_ENABLED:
                        stopScan(BluetoothConnectionState.NOT_ENABLED);
                        break;
                    case SCANNING:
                        startScan();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * Connect a specified equipment by its name
     *
     * @return
     */
    @Override
    public Single<Void> connectEquipment(final String equipmentName) {
        return Single.fromObservable(new Observable<Void>() {
            @Override
            protected void subscribeActual(Observer<? super Void> observer) {
                connectionManager.connectEquipment(equipmentName);
            }
        });
    }

    /**
     * Disconnect the currently connected equipment
     *
     * @return
     */
    @Override
    public Observable<Void> disconnectEquipment() {
        cancelPreviousConnection();
        return Observable.empty();
    }

    @Override
    public Observable<Void> clearSessionData() {
        clearEquipmentData();
        return Observable.empty();
    }

    @Override
    public Observable<Void> startProgram() {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.startProgram();
        } else {
            errorHandling(EQUIPMENT_DISCONNECTED_ERROR_CODE);
        }
        return Observable.empty();
    }

    @Override
    public Observable<Void> stopProgram() {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.stopProgram();
        } else {
            errorHandling(EQUIPMENT_DISCONNECTED_ERROR_CODE);
        }
        return Observable.empty();
    }

    /**
     * Return events about connection done changes
     *
     * @return
     */
    @Override
    public Observable<BluetoothEquipmentConnectionState> listenConnectionStates(Context context) {
        dcConnectionPublishSubject.onNext(connectionManager.getBluetoothEquipmentConnectionState());
        return dcConnectionPublishSubject.map(state -> state);
    }

    @Override
    public void onSportDataReceived(BluetoothSportStats bluetoothSportStats) {
        dcPublishSubject.onNext(bluetoothSportStats);
    }

    @Override
    public void onError(DCEquipment equipment, DCError error) {
        Timber.e("BLUETOOTH MANAGER EQUIPMENT: "
                + equipment.getName()
                + "    ERROR RECEIVED CODE: "
                + error.getDescription());
        errorHandling(error.getCode());
    }

    private void errorHandling(int errorCode) {
        BluetoothEquipmentConnectionState connectionState;
        connectionState = new BluetoothEquipmentConnectionState();

        connectionState.setConnectionState(BluetoothConnectionState.ERROR);
        connectionState.setDomyosException(ErrorUtils.mapBluetoothErrors(errorCode));
        dcConnectionPublishSubject.onNext(connectionState);
    }

    @Override
    public void onEquipmentInfoReceived(EquipmentInfo equipmentInfo) {
        //notify equipment info received (equipment version, serial number, modelId, max possible values...)
        dcEquipmentInfoPublishSubject.onNext(equipmentInfo);
    }

    @Override
    public void onConnectionStateReceived(BluetoothEquipmentConnectionState connectionState) {
        //notify connection state change
        dcConnectionPublishSubject.onNext(connectionState);
    }

    @Override
    public void onEquipmentDiscovered(Collection<DCEquipment> equipmentsDiscovered) {
        if (isBluetoothPhoneEnabled()) {
            if (oldConnectedEquipment != null && specificEquipmentManager != null && specificEquipmentManager.isProgramStarted()) {
                //in practice reconnect equipment as soon as we get it back on scan list
                connectionManager.connectEquipment(oldConnectedEquipment.getName());
            }
            //notify of a scan list change
            dcScanPublishSubject.onNext(equipmentsDiscovered);
        }
    }

    @Override
    public void onEquipmentConnected(DCEquipment connectedEquipment) {
        //Initialisation of specificEquipmentManager in case of a different equipment connected, update specific equipment otherwise

        if (this.oldConnectedEquipment == null || !this.oldConnectedEquipment.getName()
                .equals(connectedEquipment.getName())) {
            initializeSpecificEquipmentManager(connectedEquipment, true);
            //Cache du dernier equipement utilisé
            this.oldConnectedEquipment = connectedEquipment;
            //Store the last used equipment type in shared preference for StatFragment display, ELLIPTICAL by default
            //lastUsedEquimentType.set((TypeConstants.getEquipmentType(connectedEquipment)<0)? TypeConstants.TYPE_SPORT_ELLIPTIC:TypeConstants.getEquipmentType(connectedEquipment));
            //Store the selected equipment for display order purpose
            selectedEquipment = connectedEquipment.getName();
        } else {
            initializeSpecificEquipmentManager(connectedEquipment, false);
        }

        dcScanPublishSubject.onNext(Collections.singletonList(connectedEquipment));
    }

    @Override
    public void onEquipmentSearch(DCEquipment selectedEquipment) {
        //first step of connection event, we notify to refresh display
        dcScanPublishSubject.onNext(Collections.singletonList(selectedEquipment));
    }

    @Override
    public void onEquipmentDisconnected() {
        //Créer exception a remonter (regarder onError)
        if (specificEquipmentManager != null) {
            Timber.i("Equipment disconnected%s", specificEquipmentManager.getEquipmentInfo());
        }

        if (specificEquipmentManager != null && specificEquipmentManager.isProgramStarted()) {
            //A practice is launched, we need to notify that the equipment is no more connected
            BluetoothEquipmentConnectionState connectionState;
            connectionState = new BluetoothEquipmentConnectionState();

            if (isBluetoothPhoneEnabled()) {
                //disconnection due to unwanted rejection or auto powered equipment shutdown
                specificEquipmentManager.pauseClicked(PauseCauseEnum.EQUIPMENT_DISCONNECTED);
                connectionState.setDomyosException(
                        new DomyosException(DomyosException.DomyosExceptionCode.BluetoothConnectionError));
                onEquipmentPause(new EquipmentPauseState(true, PauseCauseEnum.EQUIPMENT_DISCONNECTED));
            } else {

                //disconnection due to bluetooth disconnected
                specificEquipmentManager.pauseClicked(BLUETOOTH_DISCONNECTED);
                connectionState.setDomyosException(new DomyosException(
                        DomyosException.DomyosExceptionCode.BluetoothNotEnabledConnectionError));
                onEquipmentPause(new EquipmentPauseState(true, BLUETOOTH_DISCONNECTED));
            }

            //notify registered Rx Subject of the reason of the disconnection
            dcConnectionPublishSubject.onNext(connectionState);
        }

        //we launch a scan in practice after a disconnection to attempt reconnecting or in home to get the scanned list again
        if (isBluetoothPhoneEnabled() && (specificEquipmentManager != null && specificEquipmentManager.isProgramStarted() || connectionManager.disconnectionRequested()) && !connectionManager.isStartingScan()) {
            Timber.i("BLUETOOTH MANAGER DOMYOS SCAN AFTER DISCONNECT");
            startScan();
        }
    }

    /**
     * Notify the {@link BluetoothConnectionManager} that the equipment id has been received, the connection
     * <p>
     * is fully done.
     *
     * @param equipmentId id of the connected equipment
     */
    @Override
    public void onEquipmentIdReceived(String equipmentId) {
        firstEquipmentID = equipmentId;
        connectionManager.equipmentIdReceived();
    }

    /**
     * Notify of an equipment pause state
     */
    @Override
    public void onEquipmentPause(EquipmentPauseState equipmentPauseState) {
        dcPausePublishSubject.onNext(equipmentPauseState);
    }

    /**
     * Notify if the phone, tablet is on the equipment proximity detection tab
     */
    @Override
    public void onEquipmentTabChange(boolean onTab) {
        if (onTab) {
            dcTabPublishSubject.onNext(new EquipmentPauseState(true, TAB_DETECTED));
        } else {
            dcTabPublishSubject.onNext(new EquipmentPauseState(false, TAB_NOT_DETECTED));
        }
    }

    /**
     * Used to know if the bluetooth phone is turned on and supported by device
     */
    public static boolean isBluetoothPhoneEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public static boolean isConnected(DCEquipment dcEquipment) {
        return BluetoothManagerDemo.isBluetoothPhoneEnabled() && dcEquipment != null && dcEquipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected;
    }

    /**
     * Start scanning (disconnect if necessary) for equipments with auto close after a certain period of time if not connecting equipment
     */
    public void startScan() {
        if (connectionManager.isNotProcessingCall()) {
            connectionManager.setScanRetryCount(0);
            connectionManager.setNotInitializedScanRetryCount(0);
            connectionManager.startScan();
        } else {
            dcScanPublishSubject.onNext(
                    Collections.singletonList(connectionManager.getEquipmentConnectionTry()));
        }
    }

    /**
     * Stop scanning equipments if the scan is launched
     */
    public void stopScan(BluetoothConnectionState connectionState) {
        connectionManager.stopScan(connectionState);
    }

    /**
     * Create the manager corresponding to the given equipment type to use it's specific interactions
     * <p>
     * with console and get specific data from the equipment, each represents all the equipment
     * <p>
     * available controls and data
     */
    public void initializeSpecificEquipmentManager(DCEquipment equipment, boolean isNewEquipment) {
        // float weight = profileDao.getProfileWeight(ldIdPreference.get());
        if (isNewEquipment) {
   /*   if (equipment instanceof DCBike) {
        specificEquipmentManager =
          //new BluetoothBikeManager(this, (DCBike) equipment, weight, unitsPreference.get());
                new BluetoothBikeManager(this, (DCBike) equipment, 0, true);

      } else*/
            if (equipment instanceof DCTreadmill) {
                specificEquipmentManager =
                        // new BluetoothTreadmillManager(this, (DCTreadmill) equipment, weight,unitsPreference.get());
                        new BluetoothTreadmillManager(this, (DCTreadmill) equipment, 0, true);
            }/* else if (equipment instanceof DCEllipticalTrainer) {
        specificEquipmentManager =
         // new BluetoothEllipticalManager(this, (DCEllipticalTrainer) equipment, weight, unitsPreference.get());
                new BluetoothEllipticalManager(this, (DCEllipticalTrainer) equipment, 0, true);
      } else if (equipment instanceof DCRower) {
        specificEquipmentManager =
         // new BluetoothRowerManager(this, (DCRower) equipment, weight, unitsPreference.get());
                new BluetoothRowerManager(this, (DCRower) equipment, 0,true);
      }*/
        } else {
      /*if (specificEquipmentManager instanceof BluetoothBikeManager) {
        ((BluetoothBikeManager) specificEquipmentManager).initialize((DCBike) equipment, false);
      } else */
            if (specificEquipmentManager instanceof BluetoothTreadmillManager) {
                ((BluetoothTreadmillManager) specificEquipmentManager).initialize((DCTreadmill) equipment,
                        false);
            } /*else if (specificEquipmentManager instanceof BluetoothEllipticalManager) {
        ((BluetoothEllipticalManager) specificEquipmentManager).initialize(
          (DCEllipticalTrainer) equipment, false);
      } else if (specificEquipmentManager instanceof BluetoothRowerManager) {
        ((BluetoothRowerManager) specificEquipmentManager).initialize((DCRower) equipment, false);
      }*/
        }
    }

    /**
     * Unregister callbacks and disconnect from previous equipment, no more command GET will be sent
     * <p>
     * to the equipment
     */
    private void cancelPreviousConnection() {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.clearSportDataListeners();
        }

        connectionManager.cancelPreviousConnection();

        if (connecteForFirstEquipmentID) {
            connecteForFirstEquipmentID = false;
        }
    }

    /**
     * Clear all session data and reset values
     */
    private void clearEquipmentData() {
        if (specificEquipmentManager != null) {
            specificEquipmentManager.clearSessionData();
        }
    }

    /**
     * return equipment type of the equipment
     */
    private int getEquipmentType(DCEquipment equipment) {
        return TypeConstants.getEquipmentType(equipment);
    }

    /**
     * Transform a {@link Collection<  DCEquipment  >} to {@link List<BluetoothDiscoveredEquipment>}
     * <p>
     * Used to sort scanned equipment list by RSSI for proximity order also.
     */
    private List<BluetoothDiscoveredEquipment> transformEquipmentList(
            Collection<DCEquipment> dcEquipments) {

        List<DCEquipment> equipList = new ArrayList<>(dcEquipments);
        //sort scanned equipments by rssi
        Collections.sort(equipList, (o1, o2) -> o2.getScanningRSSI() - o1.getScanningRSSI());

        List<BluetoothDiscoveredEquipment> discoveredEquipments = new ArrayList<>();

        BluetoothDiscoveredEquipment selected = new BluetoothDiscoveredEquipment();
        if (connectionManager.isNotProcessingCall() && !connectionManager.isEquipmentConnected()) {
            //we are not connected to equipment, make the selected equipment as the first of the list regardless of the RSSI
            for (DCEquipment equipment : equipList) {
                if (selectedEquipment == null || !equipment.getName().equals(selectedEquipment)) {
                    BluetoothDiscoveredEquipment discoveredEquipment = new BluetoothDiscoveredEquipment();
                    discoveredEquipment.setEquipmentName(equipment.getName());
                    discoveredEquipment.setEquipmentType(getEquipmentType(equipment));
                    discoveredEquipments.add(discoveredEquipment);
                } else {
                    if (selectedEquipment != null) {
                        selected.setEquipmentName(equipment.getName());
                        selected.setEquipmentType(getEquipmentType(equipment));
                        discoveredEquipments.add(0, selected);
                    }
                }
            }
        } else {
            //we are connecting equipment or connected, only the concerned equipment has to be displayed
            if (connectionManager.getBluetoothEquipmentConnectionState().getConnectedEquipment() != null) {
                selected.setEquipmentName(
                        connectionManager.getBluetoothEquipmentConnectionState().getConnectedEquipment().getName());
                selected.setEquipmentType(getEquipmentType(
                        connectionManager.getBluetoothEquipmentConnectionState().getConnectedEquipment()));
            } else {
                selected.setEquipmentName(connectionManager.getEquipmentConnectionTry()
                        .getName());
                selected.setEquipmentType(getEquipmentType(
                        connectionManager.getEquipmentConnectionTry()));
            }
            discoveredEquipments.add(0, selected);
        }
        return discoveredEquipments;
    }
}
