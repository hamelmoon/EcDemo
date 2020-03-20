package com.sh.ec.bluetooth.manager.connection;

import android.content.Context;
import android.util.Log;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentManager;
import com.appdevice.domyos.DCEquipmentManager.DCEquipmentManagerCallback;
import com.sh.ec.bluetooth.BluetoothConnectionState;
import com.sh.ec.bluetooth.common.BluetoothEquipmentConnectionState;
import com.sh.ec.bluetooth.manager.BluetoothManager;
import com.sh.ec.bluetooth.manager.BluetoothSpecificManager;
import com.sh.ec.bluetooth.manager.ManagerEventListener;
import com.sh.ec.utils.LogUtils;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;


public class BluetoothConnectionManager implements BluetoothSpecificManager, DCEquipmentManagerCallback {
   private static final int SCAN_MAX_RETRY_COUNT = 5;
   private static final int RECONNECT_MAX_RETRY_COUNT = 5;
   private static final int DEFAULT_CANCEL_CONNECTION_DELAY = 500;
   private static final long DEFAULT_BLUETOOTH_TIMEOUT_ELAPSED_TIME = 25000;//DEFAULT_CANCEL_CONNECTION_DELAY before connection timeout
   private static final long DEFAULT_BLUETOOTH_TIMEOUT_ELAPSED_TIME_WITH_EQUIPMENT_SEARCH = 30000;//DEFAULT_CANCEL_CONNECTION_DELAY before equipment availability search timeout
   private static final int NOT_INITIALIZED_SCAN_RETRY_DELAY_VALUE = 1000;//time window to check whether the scan has been stopped or not for connection
   private static final int SCAN_DELAY_VALUE = 500;//used for scan retry count purpose

   private ManagerEventListener mListener;
   private final DCEquipmentManager dcEquipmentManager;//sdk manager to communicate with equipment and receive callbacks
   private DCEquipment connectedEquipment;//equipment currently connected
   private DCEquipment equipmentConnectionTry;//equipment we are searching for connection

   private boolean waitConnectionId;//lock for connection final step (id + workout)
   private boolean waitResponse;//lock for connection first step (availability + connecting)
   private boolean verifyingEquipmentAvailable = false;//lock for availability search
   private BluetoothConnectionState bluetoothConnectionState =
       BluetoothConnectionState.NOT_INITIALIZED;//current connection state
   private final BluetoothEquipmentConnectionState bluetoothEquipmentConnectionState =
       new BluetoothEquipmentConnectionState();//object sent as notify event


   private int scanRetryCount = 0;
   private int notInitializedScanRetryCount = 0;
   private int reConnectionRetryCount = 0;

   private boolean wantDisconnection = false;//determine if a disconnection is wanted or is an error

   private boolean startingScan = false;//determine if a scan launch is occurring

   private Timer timeoutConnectionTimer = new Timer();

  public BluetoothConnectionManager(DCEquipmentManager dcEquipmentManager, Context context) {
       this.dcEquipmentManager = dcEquipmentManager;
       refreshConnectionModelValue();
       init(this, context);
       notifyManager();
   }

   @Override
   public void equipmentManagerDidUnBound() {
       LogUtils.d("|---------------BLUETOOTH MANAGER DOMYOS DID INITIALIZED-------------|---equipmentManagerDidUnBound");

   }

   /**
    * Called after equipment manager initialisation
    */
   @Override public synchronized void equipmentManagerDidInitialized() {
       LogUtils.d("|---------------BLUETOOTH MANAGER DOMYOS DID INITIALIZED-------------|");
       bluetoothConnectionState = BluetoothConnectionState.INITIALIZED;
       notInitializedScanRetryCount = 0;
       waitResponse = false;
       verifyingEquipmentAvailable = false;
       notifyManager();
   }

   /**
    * Called when a new equipment has been discovered
    *
    * @param dcEquipment equipment discovered, beware it can be null
    */
   @Override public synchronized void equipmentManagerDidDiscoverEquipment(DCEquipment dcEquipment) {
       startingScan = false;
       if (!verifyingEquipmentAvailable) {
           //we are not connected notify of a new scan result
           try {
               if (dcEquipment.getName() != null) {
                   Log.e("BLUETOOTH", dcEquipment.getName()+"");
               }
               if (bluetoothConnectionState != null
                   && bluetoothConnectionState != BluetoothConnectionState.CONNECTED
                   && bluetoothConnectionState != BluetoothConnectionState.WAITING_CONNECTION_ID) {
                   bluetoothConnectionState = BluetoothConnectionState.SCANNING;
               }
               notifyManager();
           } catch (Exception e) {

           }
       } else {
           //we are attempting to connect an equipment, notify of the equipment availability search
           LogUtils.d("BLUETOOTH MANAGER DOMYOS SCAN RESULT, CONNECTION IS PROCESSING");

           LogUtils.d("BLUETOOTH MANAGER DOMYOS EQUIPMENT AVAILABLE CHECK");
           if (equipmentConnectionTry != null && dcEquipment.getName()
               .equals(equipmentConnectionTry.getName())) {
               LogUtils.d("BLUETOOTH MANAGER DOMYOS EQUIPMENT AVAILABLE CHECK : SUCCESS, TRY CONNECTION");
               //equipment still available
               verifyingEquipmentAvailable = false;
               launchManagerConnection(equipmentConnectionTry.getName(),
                   equipmentConnectionTry, true);
           }
       }
   }

   /**
    * Called when an equipment has been connected and notify that the equipment id is still needed
    *
    * in order to be fully connected. Reset locks because the equipment response is received.
    */
   @Override public synchronized void equipmentManagerDidConnectEquipment(DCEquipment dcEquipment) {
       stopScan(null);
       connectedEquipment = dcEquipment;
       waitConnectionId = true;
       //cancel any pending timeout to avoid timer overlap
       purgeTimeoutTimerIfAny();
       waitResponse = false;
       verifyingEquipmentAvailable = false;
       wantDisconnection = false;
       bluetoothConnectionState = BluetoothConnectionState.WAITING_CONNECTION_ID;
       notifyManager();
   }

   /**
    * Called when an equipment has been disconnected and notify whether it is a predicted disconnection
    *
    * or an equipment rejection. Reset the locking booleans.
    */
   @Override public synchronized void equipmentManagerDidDisconnectEquipment(DCEquipment dcEquipment) {
       //cancel any pending timeout to avoid timer overlap
       purgeTimeoutTimerIfAny();
       waitResponse = false;
       verifyingEquipmentAvailable = false;
       if (wantDisconnection) {
           waitConnectionId = false;
           connectedEquipment = null;
           bluetoothConnectionState = BluetoothConnectionState.DISCONNECTED;
           notifyManager();
       } else {
           waitConnectionId = false;
           connectedEquipment = null;
           LogUtils.d(
               "BLUETOOTH MANAGER DOMYOS DID DISCONNECT EQUIPMENT : UNWANTED REJECTION BY EQUIPMENT OR SDK");

           if(BluetoothManager.isBluetoothPhoneEnabled() && reConnectionRetryCount>0){
               reConnectionRetryCount--;
               launchManagerConnection(equipmentConnectionTry.getName(),
                   equipmentConnectionTry, true);
           }else {
               bluetoothConnectionState = BluetoothConnectionState.REJECTED_BY_EQUIPMENT;
               notifyManager();
           }
       }
   }

   @Override public void notifyManager() {
       if (mListener != null) {
           refreshConnectionModelValue();
           mListener.onConnectionStateReceived(bluetoothEquipmentConnectionState);

           switch (bluetoothConnectionState) {
               case SCANNING:
                   mListener.onEquipmentDiscovered(dcEquipmentManager.getEquipments());
                   break;
               case WAITING_CONNECTION_ID:
                   mListener.onEquipmentConnected(connectedEquipment);
                   break;
               case WAITING_SELECTED_EQUIPMENT_DETECTION:
                   mListener.onEquipmentSearch(equipmentConnectionTry);
                   break;
               case DISCONNECTED:
                   mListener.onEquipmentDisconnected();
                   break;
               case REJECTED_BY_EQUIPMENT:
                   mListener.onEquipmentDisconnected();
                   break;
               default:
                   break;
           }
       }
   }

   /**
    * @return the current connection state
    */
   public BluetoothEquipmentConnectionState getBluetoothEquipmentConnectionState() {
       return bluetoothEquipmentConnectionState;
   }

   /**
    * @return the sdk scanned equipment list
    */
   public Collection<DCEquipment> getEquipments() {
       return dcEquipmentManager.getEquipments();
   }

   /**
    * Start scanning for equipments, first we check manager initialization, then stop scan
    *
    * and then we launch the new scan. We first stop any pending scan in order to force
    *
    * the refresh of the sdk scanned equipment list that have by default a time buffer too long
    *
    * before refresh.
    *
    * Function launchManagerConnection() uses the same kind of behaviour to search for selected equipment availability
    */
   public void startScan() {
       startingScan = true;
       LogUtils.d("BLUETOOTH MANAGER DOMYOS START SCAN ASKED");
       if (dcEquipmentManager.getInitializationState()) {
           Timer launchScanTimer = new Timer();
           notInitializedScanRetryCount = 0;
           LogUtils.d("BLUETOOTH MANAGER DOMYOS STOP ANY SCAN BEFORE STARTING SCAN");
           stopScan(null);

           launchScanTimer.schedule(new TimerTask() {
               @Override public void run() {
                   if (!dcEquipmentManager.isScanning()) {
                       LogUtils.d("BLUETOOTH MANAGER DOMYOS NO SCAN LAUNCHED");

                       scanRetryCount = 0;
                       launchScan();
                   } else {
                       if (scanRetryCount < SCAN_MAX_RETRY_COUNT) {
                           LogUtils.d(
                               "BLUETOOTH MANAGER DOMYOS STILL SCANNING RETRYING START SCAN LAUNCH --> RETRY COUNT : %d/%d"
                               +scanRetryCount +"-----"+ SCAN_MAX_RETRY_COUNT);
                           scanRetryCount++;
                           startScan();
                       } else {
                           startingScan = false;
                           LogUtils.d(
                               "BLUETOOTH MANAGER DOMYOS CAN'T CANCEL SCAN TOO MANY RETRY ASKED");
                       }
                   }
               }
           }, SCAN_DELAY_VALUE);
       } else {
           if (notInitializedScanRetryCount <= SCAN_MAX_RETRY_COUNT) {

               Timer retryScanTimer = new Timer();

               LogUtils.d("BLUETOOTH MANAGER DOMYOS NOT INITIALIZED TRY DELAYED SCAN LAUNCH ...");
               retryScanTimer.schedule(new TimerTask() {
                   @Override public void run() {
                       if (notInitializedScanRetryCount <= SCAN_MAX_RETRY_COUNT) {
                           Log.e(
                               "BLUETOOTH",
                               notInitializedScanRetryCount+"-----"+ SCAN_MAX_RETRY_COUNT);
                           notInitializedScanRetryCount++;
                           startScan();
                       } else {
                           startingScan = false;
                           Log.e("BLUETOOTH","---------");
                       }
                   }
               }, NOT_INITIALIZED_SCAN_RETRY_DELAY_VALUE);
           } else {
               startingScan = false;
              Log.e(
                   "BLUETOOTH","--------");
           }
       }
   }

   /**
    * Stop scanning equipments if the scan is launched
    * @param connectionState state to be notified or no event sent if null
    */
   public void stopScan(BluetoothConnectionState connectionState) {
       if (!BluetoothManager.isBluetoothPhoneEnabled()) {
           LogUtils.d("BLUETOOTH MANAGER DOMYOS ASK SCAN STOP BUT BLUETOOTH ADAPTER NOT ENABLED");
       } else {
           if (dcEquipmentManager.getInitializationState() && dcEquipmentManager.isScanning()) {
               LogUtils.d("BLUETOOTH MANAGER DOMYOS ASK SCAN STOP");
               dcEquipmentManager.stopScanEquipments();
           }
       }

       if (connectionState != null) {
           bluetoothConnectionState = connectionState;
           notifyManager();
       }
   }

   /**
    * Disconnect from previous equipment if we can
    */
   public void cancelPreviousConnection() {
       waitConnectionId = false;
       waitResponse = false;
       if (BluetoothManager.isBluetoothPhoneEnabled()
           && dcEquipmentManager.getInitializationState()
           && isEquipmentConnected()) {
           //always an expected disconnection
           wantDisconnection = true;
           LogUtils.d("BLUETOOTH MANAGER DOMYOS CANCEL PREVIOUS CONNECTION ASKED : INITIALIZE TIMER TO FINISH PENDING COMMANDS");
           Timer launchScanTimer = new Timer();
           launchScanTimer.schedule(
               new TimerTask() {
                   @Override public void run() {
                       LogUtils.d("BLUETOOTH MANAGER DOMYOS CANCEL PREVIOUS CONNECTION ASKED : TIME TO DISCONNECT REACHED");
                       if (BluetoothManager.isBluetoothPhoneEnabled()
                           && dcEquipmentManager.getInitializationState()
                           && isEquipmentConnected()) {
                           LogUtils.d("BLUETOOTH MANAGER DOMYOS CANCEL PREVIOUS CONNECTION ASKED : ASKING DISCONNECT");
                           dcEquipmentManager.cancelEquipmentConnection(connectedEquipment);
                       } else {
                           LogUtils.d("BLUETOOTH MANAGER DOMYOS CANCEL PREVIOUS CONNECTION ASKED : CAN'T DISCONNECT");
                       }
                   }
               },DEFAULT_CANCEL_CONNECTION_DELAY);
       } else {
           LogUtils.d(
               "BLUETOOTH MANAGER DOMYOS CANCEL PREVIOUS CONNECTION CAN'T BE FULFILLED DUE TO MANAGER NOT INITIALIZED, CONNECTING OR ALREADY DISCONNECTED");
       }
   }

   /**
    * Retrieve and connect the specified equipment by its name if the equipment can be connected and no
    *
    * other tries are occurring.
    */
   public void connectEquipment(final String equipmentName) {
       LogUtils.d("BLUETOOTH MANAGER DOMYOS CHECK CONNECTION STATUS TO CONNECT EQUIPMENT");
       if (canConnectEquipment()) {
           DCEquipment[] equipments = new DCEquipment[dcEquipmentManager.getEquipments().size()];
           dcEquipmentManager.getEquipments().toArray(equipments);
           //connect to the first equipment retrieved by default or to the given one
           if ((equipmentName == null || equipmentName.equals(""))
               && dcEquipmentManager.getEquipments().size() > 0) {
               LogUtils.d("BLUETOOTH MANAGER DOMYOS CONNECTION TRY EMPTY OR NULL EQUIPMENT NAME : CONNECTING FIRST EQUIPMENT SEEN");
               launchManagerConnection(equipments[0].getName(), equipments[0], false);
           } else {
               LogUtils.d("BLUETOOTH MANAGER DOMYOS CONNECTION TRY EQUIPMENT NAME SEARCH IN SCANNED LIST");
               DCEquipment matchingEquipment = searchEquipment(equipments, equipmentName);
               if (matchingEquipment!=null) {
                   LogUtils.d("BLUETOOTH MANAGER DOMYOS CONNECTION TRY EQUIPMENT NAME SEARCH IN SCANNED LIST : SUCCESS");
                   launchManagerConnection(equipmentName, matchingEquipment, false);
               }else{
                   LogUtils.d("BLUETOOTH MANAGER DOMYOS CONNECTION TRY EQUIPMENT NAME SEARCH IN SCANNED LIST : FAIL");
               }
           }
       }else{
           LogUtils.d("BLUETOOTH MANAGER DOMYOS CHECK CONNECTION STATUS : FAIL");
       }
   }

   /**
    * used to know if the manager can be safely called to avoid equipment connection request spam issues
    *
    * @return true if no connection is pending, or if a timeout has been reached
    */
   public boolean isNotProcessingCall() {
       return !waitResponse;
   }

   /**
    * Used to know if a disconnection request was previously requested by app (which can be handled),
    *
    * or by a result of the bluetooth sdk behaviour (can't be predicted and should be considered as an error)
    */
   public boolean disconnectionRequested() {
       return wantDisconnection;
   }

   /**
    * Change to connected state when the equipment id is received, we have all necessary
    *
    * data to start a session and we are sure that the equipment is in workout mode
    */
   public void equipmentIdReceived() {
       if (waitConnectionId) {
           waitConnectionId = false;
           bluetoothConnectionState = BluetoothConnectionState.CONNECTED;
           notifyManager();
       }
   }

   /**
    * Used to register to {@link BluetoothConnectionManager} fired events through
    *
    * notify() Function
     */
   public void setListener(ManagerEventListener mListener) {
       this.mListener = mListener;
   }

   /**
    * Used to reset scan retry count when we fail to start scan because of a fail to stop a previous scan
    */
   public void setScanRetryCount(int scanRetryCount) {
       this.scanRetryCount = scanRetryCount;
   }

   /**
    * Used to reset scan retry count when the manager can't scan because not initialized
    */
   public void setNotInitializedScanRetryCount(int notInitializedScanRetryCount) {
       this.notInitializedScanRetryCount = notInitializedScanRetryCount;
   }

   /**
    * Return the equipment that we search to know if it is available
    */
   public DCEquipment getEquipmentConnectionTry() {
       return equipmentConnectionTry;
   }

   /**
    * Used to know if a scan try is pending or not
    */
   public boolean isStartingScan() {
       return startingScan;
   }

   /**
    * Initialize the equipment manager and listen to connections callbacks
    */
   private void init(DCEquipmentManagerCallback listener, Context context) {
       dcEquipmentManager.initialize(context);
       dcEquipmentManager.setCallBack(listener);
   }

   /**
    * Start scanning for equipments if not already connected and notify of scanning state,
    *
    * cancel connection otherwise (another scan will be fired on disconnection see {@link BluetoothManager})
    */
   private void launchScan() {
       if (isEquipmentConnected()) {
           LogUtils.d(
               "BLUETOOTH MANAGER DOMYOS WANT SCAN LAUNCH BUT CONNECTED -> CANCEL PREVIOUS CONNECTION");
           startingScan = false;
           cancelPreviousConnection();
       } else {
           LogUtils.d("BLUETOOTH MANAGER DOMYOS LAUNCHING SCANNING PROCESS");
           if(dcEquipmentManager.getInitializationState()) {
               dcEquipmentManager.scanEquipments();
               if (!verifyingEquipmentAvailable) {
                   bluetoothConnectionState = BluetoothConnectionState.SCANNING;
                   notifyManager();
               }
           }
       }
   }

   /**
    * Function used to launch a connection, first we ensure that the equipment is available and lock any other connections,
    *
    * by scanning it again. Then, we stop any scan before attempting to connect the equipment.
    *
    * @param equipmentName name of the equipment to connect to
    * @param equipmentToConnect equipment to connect
    * @param canConnect determine whether the equipment is available or not
    */
   private void launchManagerConnection(String equipmentName, DCEquipment equipmentToConnect,
       boolean canConnect) {
       //lock any other tries to connect equipment
       waitResponse = true;
       wantDisconnection = false;
       if (canConnect) {
           connectedEquipment = equipmentToConnect;
           stopScan(null);
           Timer timer = new Timer();
           timer.schedule(new TimerTask() {
               @Override public void run() {
                   if (!dcEquipmentManager.isScanning()) {
                       LogUtils.d("BLUETOOTH MANAGER DOMYOS ASK CONNECTION TO : %s"+ equipmentName);

                       LogUtils.d("BLUETOOTH MANAGER DOMYOS TIMEOUT INITIALIZED");
                       launchTimeOutTimerTask(DEFAULT_BLUETOOTH_TIMEOUT_ELAPSED_TIME);
                       dcEquipmentManager.connectEquipment(equipmentToConnect);
                   } else {
                       launchManagerConnection(equipmentName, equipmentToConnect, true);
                   }
               }
           }, 1000);
       } else {
           //notify of the search of equipment availability
           LogUtils.d("BLUETOOTH MANAGER DOMYOS ASK IF EQUIPMENT AVAILABLE BY REFRESH SCAN: %s"+ equipmentName);
           if (!verifyingEquipmentAvailable) {
               reConnectionRetryCount = RECONNECT_MAX_RETRY_COUNT;
               bluetoothConnectionState =
                   BluetoothConnectionState.WAITING_SELECTED_EQUIPMENT_DETECTION;
               notifyManager();
               //timeout task for the equipment availability search
               launchTimeOutTimerTask(
                   DEFAULT_BLUETOOTH_TIMEOUT_ELAPSED_TIME_WITH_EQUIPMENT_SEARCH);
               verifyingEquipmentAvailable = true;
               equipmentConnectionTry = equipmentToConnect;
               startScan();
           }else{
               LogUtils.d("BLUETOOTH MANAGER DOMYOS ASK IF EQUIPMENT AVAILABLE FAIL : ALREADY CHECKING");
           }
       }
   }

   /**
    * Launch a timeout task that will notify with a DEFAULT_CANCEL_CONNECTION_DELAY of a timeout and will enable new connection again
    * @param delay in millis before the timeout
    */
   private synchronized void launchTimeOutTimerTask(long delay) {
       //first cancel any pending timeout to avoid timer overlap
       purgeTimeoutTimerIfAny();
       timeoutConnectionTimer = new Timer();
       timeoutConnectionTimer.schedule(new TimerTask() {
           @Override public void run() {
               //case when no response has been sent (connection or disconnection)
               //enable connection again
               waitResponse = false;
               verifyingEquipmentAvailable = false;
               LogUtils.d(
                   "BLUETOOTH MANAGER DOMYOS TIMEOUT REACHED, RELEASING LOCKS ...");
               //equipment still not connected and no response, notify timeout and clear connection try
               if ((connectedEquipment == null
                   || (connectedEquipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateDisconnected))) {
                   LogUtils.d(
                       "BLUETOOTH MANAGER DOMYOS TIMEOUT REACHED, CANCELLING CONNECTION REQUEST ...");
                   cancelPreviousConnection();
                   bluetoothConnectionState = BluetoothConnectionState.TIME_OUT;
                   notifyManager();
               }
           }
       }, delay);
   }

   /**
    * Determine if an equipment is connected or connecting
    */
   public boolean isEquipmentConnected() {
       return connectedEquipment != null && (connectedEquipment.getConnectionState()
           == DCEquipment.DCEquipmentConnectionStateConnected
           || connectedEquipment.getConnectionState()
           == DCEquipment.DCEquipmentConnectionStateConnecting);
   }

   /**
    * Refresh the current connection state model with the corresponding values
    */
   private void refreshConnectionModelValue() {
       bluetoothEquipmentConnectionState.setConnectionState(bluetoothConnectionState);
       bluetoothEquipmentConnectionState.setEquipmentNumber(
           dcEquipmentManager.getEquipments().size());
       bluetoothEquipmentConnectionState.setConnectedEquipment(connectedEquipment);
   }

   /**
    * Function that search for the equipment with the given name
    * @param equipments list of available equipments
    * @param equipmentName name of the equipment to search
    * @return the {@link DCEquipment} matched
    */
   private DCEquipment searchEquipment(DCEquipment[] equipments, String equipmentName) {
       if (equipments != null && equipments.length > 0) {
           for (final DCEquipment equipment : equipments) {
               if (equipment.getName().equals(equipmentName)) {
                   return equipment;
               }
           }
           return null;
       } else {
           return null;
       }
   }

   /**
    * Function used to know if a connection can be launched
    */
   private boolean canConnectEquipment() {
       return dcEquipmentManager.getInitializationState() && (connectedEquipment == null
           || connectedEquipment.getConnectionState()
           == DCEquipment.DCEquipmentConnectionStateDisconnected) && isNotProcessingCall();
   }

   private synchronized void purgeTimeoutTimerIfAny(){
       if (timeoutConnectionTimer != null) {
           timeoutConnectionTimer.cancel();
           timeoutConnectionTimer.purge();
       }
   }
}
