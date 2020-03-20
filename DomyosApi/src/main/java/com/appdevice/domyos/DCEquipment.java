package com.appdevice.domyos;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.appdevice.api.ble.ADBlePeripheral;
import com.appdevice.api.ble.ADBlePeripheral.ADWriteCharacteristicCallback;
import com.appdevice.api.ble.ADBlePeripheralCallback;
import com.appdevice.api.utility.ADConverter;
import com.appdevice.api.utility.ADLog;
import com.appdevice.domyos.commands.DCELGetInclineCommand;
import com.appdevice.domyos.commands.DCGetEquipmentIDCommand;
import com.appdevice.domyos.commands.DCGetErrorHistoricListCommand;
import com.appdevice.domyos.commands.DCGetInfoValueCommand;
import com.appdevice.domyos.commands.DCGetSaleServiceCommand;
import com.appdevice.domyos.commands.DCRebootConsoleCommand;
import com.appdevice.domyos.commands.DCRebootConsoleExCommand;
import com.appdevice.domyos.commands.DCResetWorkoutResultCommand;
import com.appdevice.domyos.commands.DCSetBeepCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneOffSecondAreaCommand;
import com.appdevice.domyos.commands.DCSetDisplayZoneSecondAreaCommand;
import com.appdevice.domyos.commands.DCSetFanSpeedLevelCommand;
import com.appdevice.domyos.commands.DCSetHotKeyCommand;
import com.appdevice.domyos.commands.DCSetSaleServiceCommand;
import com.appdevice.domyos.commands.DCSetSessionDataCommand;
import com.appdevice.domyos.commands.DCUpgradeConsoleCommand;
import com.appdevice.domyos.commands.DCUpgradeConsoleEraseCommand;
import com.appdevice.domyos.commands.DCUpgradeConsoleGetIDCommand;
import com.appdevice.domyos.commands.DCUpgradeConsoleGetResetVectorCommand;
import com.appdevice.domyos.commands.DCUpgradeConsoleGetVersionCommand;
import com.appdevice.domyos.commands.DCUpgradeConsoleProgramFinishCommand;
import com.appdevice.domyos.commands.DCUpgradeConsoleProgramPicxxCommand;
import com.appdevice.domyos.parameters.DCEquipmentDisplayZoneOffSecondAreaParameters;
import com.appdevice.domyos.parameters.DCEquipmentDisplayZoneSecondAreaParameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class DCEquipment {

    public interface DCEquipmentListener {

        void equipmentTabOnEquipmentChanged(DCEquipment equipment, boolean tabOnEquipment);

        void equipmentErrorOccurred(DCEquipment equipment, int errorNumber);

        void equipmentPressedButtonChanged(DCEquipment equipment, int pressedButton);

        void equipmentOnHotKeyStatusChanged(DCEquipment equipment, int hotKeyStatus);

        void equipmentOnFanSpeedLevelChanged(DCEquipment equipment, int fanSpeedLevel);

    }

    private static final String TAG = "DCEquipment";

    private static final UUID SERVICE_UUID = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
    private static final UUID WRITE_UUID = UUID.fromString("49535343-8841-43F4-A8D4-ECBE34729BB3");
    private static final UUID NOTIFY_UUID = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");

    public static final int DCSessionDataTypeClear = 3;

    public static final int DCHeartRateColorOff = 0;
    public static final int DCHeartRateColorBlue = 1;
    public static final int DCHeartRateColorGreen = 2;
    public static final int DCHeartRateColorYellow = 3;
    public static final int DCHeartRateColorOrange = 4;
    public static final int DCHeartRateColorRed = 5;

    public static final int DCBtLedSwitchOff = 0;
    public static final int DCBtLedSwitchOn = 1;

    public static final int DCFanSpeedLevelOff = 0;
    public static final int DCFanSpeedLevel1 = 1;
    public static final int DCFanSpeedLevel2 = 2;
    public static final int DCFanSpeedLevel3 = 2;
    public static final int DCFanSpeedLevel4 = 4;
    public static final int DCFanSpeedLevel5 = 5;

    public static final int DCEquipmentConnectionStateDisconnected = 0;
    public static final int DCEquipmentConnectionStateConnecting = 1;
    public static final int DCEquipmentConnectionStateConnected = 2;

    public static final int DCEquipmentModeSetting = 1;
    public static final int DCEquipmentModeWorkout = 2;

    public static final int DCHotKeyPause = 0;
    public static final int DCHotKeyStart = 1;

    public static final int DCErrorCodeNotConnected = 100;
    public static final int DCErrorCodeWrongMode = 101;
    public static final int DCErrorCodeWrongParameter = 102;
    public static final int DCErrorCodeChangeMode = 103;
    public static final int DCErrorCodeRetryCountExceeded = 104;
    public static final int DCErrorCodeRequestDenied = 105;

    public static final int DCErrorCodeUpgradeConsoleIncorrectFile = 1000;
    public static final int DCErrorCodeUpgradeConsoleConnectTimeout = 1001;
    public static final int DCErrorCodeUpgradeConsolePICsVersionTimeout = 1002;
    public static final int DCErrorCodeUpgradeConsoleResetVectorReadTimeout = 1003;
    public static final int DCErrorCodeUpgradeConsoleEraseTimeout = 1004;
    public static final int DCErrorCodeUpgradeConsoleFlashWriting = 1005;
    public static final int DCErrorCodeUpgradeConsoleVerifyTimeout = 1006;
    public static final int DCErrorCodeUpgradeConsoleVerifyFailed = 1007;
    public static final int DCErrorCodeUpgradeConsoleCMDFinishTimeout = 1008;

    int mMode;
    private int mHotKeyStatus;
    private int mFanSpeedLevel;

    private boolean mTabOnEquipment;
    boolean mAutoLinkBackEnable;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mSetNotifySuccess = false;
    private int mErrorNumber;
    private int mPressedButton = 0;
    private boolean mInMaintainMenu = false;
    ADBlePeripheral mPeripheral = null;
    boolean mIsManualDisconnect = false;
    private boolean mIsUpdatingConsole;

    private static final Timer mTimer = new Timer();
    private static final Timer inclineTimer = new Timer();
    private Queue<DCCommand> mCommands = new ConcurrentLinkedQueue<DCCommand>();
    private DCCommand mCurrentCommand = null;
    private int mRetryCount = -1;
    private final Handler mHandler = new Handler();
    private final ByteBuffer mReceiveData = ByteBuffer.allocate(128);
    private Class<?> mLastAddCommandClass = null;

    private int mScanningRSSI = -1;


    protected DCEquipmentListener mListener;

    public DCEquipment() {
        mTimer.schedule(new ADTimerTask(), 100, 300);
       // inclineTimer.schedule(new GetInclineTask(), 450, 300);
    }

    public String getName() {
        if (mPeripheral.getName() == null) {
            return "";
        }
        return mPeripheral.getName();
    }

    public String getAddress() {
        if (mPeripheral.getAddress() == null)
            return "";
        return mPeripheral.getAddress();
    }

    public int getConnectionState() {
        if (this.mWriteCharacteristic == null || this.mNotifyCharacteristic == null || !mSetNotifySuccess) {
            return DCEquipmentConnectionStateDisconnected;
        }
        return this.mPeripheral.getBleConnectionStatus();
    }

    public int getMode() {
        return this.mMode;
    }

    public int getHotKeyStatus() {
        return this.mHotKeyStatus;
    }

    protected void setHotKeyStatus(int hotKeyStatus) {
        if (mHotKeyStatus != hotKeyStatus) {
            mHotKeyStatus = hotKeyStatus;

            if (mListener != null) {
                mListener.equipmentOnHotKeyStatusChanged(this, hotKeyStatus);
            }
        }
    }

    public int getFanSpeedLevel() {
        return this.mFanSpeedLevel;
    }

    protected void setFanSpeedLevel(int fanSpeedLevel) {
        if (mFanSpeedLevel != fanSpeedLevel) {
            mFanSpeedLevel = fanSpeedLevel;

            if (mListener != null) {
                mListener.equipmentOnFanSpeedLevelChanged(this, fanSpeedLevel);
            }
        }
    }

    public boolean getTabOnEquipment() {
        return this.mTabOnEquipment;
    }

    protected void setTabOnEquipment(boolean tabOnEquipment) {
        if (this.mTabOnEquipment != tabOnEquipment) {
            this.mTabOnEquipment = tabOnEquipment;

            if (mListener != null) {
                mListener.equipmentTabOnEquipmentChanged(this, tabOnEquipment);
            }
        }
    }

    public int getPressedButton() {
        return this.mPressedButton;
    }

    protected void setPressedButton(int pressedButton) {
        if (mPressedButton != pressedButton) {
            mPressedButton = pressedButton;

            if (pressedButton != 0 && mListener != null) {
                mListener.equipmentPressedButtonChanged(this, pressedButton);
            }
        }
    }

    public boolean getAutoLinkBackEnable() {
        return this.mAutoLinkBackEnable;
    }

    public void setAutoLinkBackEnable(boolean autoLinkBackEnable) {
        this.mAutoLinkBackEnable = autoLinkBackEnable;
    }

    int getErrorNumber() {
        return mErrorNumber;
    }

    void setErrorNumber(int errorNumber) {
        if (mErrorNumber != errorNumber) {
            mErrorNumber = errorNumber;

            if (mListener != null) {
                mListener.equipmentErrorOccurred(this, errorNumber);
            }
            ADLog.e(TAG, "setErrorNumber %d", errorNumber);

            if (errorNumber != 200 && errorNumber != 255 && errorNumber != 10 && errorNumber != 0) {
                DCRebootConsoleExCommand rebootConsoleCommand = new DCRebootConsoleExCommand();
                addCommand(rebootConsoleCommand);
            }
        }
    }

    public int getScanningRSSI() {
        return mScanningRSSI;
    }

    public void setScanningRSSI(int scanningRSSI) {
        mScanningRSSI = scanningRSSI;
    }

    void resetEquipment() {
        this.mCommands.clear();
        this.mCurrentCommand = null;
        this.mLastAddCommandClass = null;
        this.mMode = DCEquipmentModeSetting;
        this.mErrorNumber = 0;
        this.mTabOnEquipment = false;
        this.mPressedButton = 0;
        this.mInMaintainMenu = false;
        this.mHotKeyStatus = DCHotKeyPause;
        this.mFanSpeedLevel = DCFanSpeedLevelOff;
        this.mSetNotifySuccess = false;
        this.mIsUpdatingConsole = false;
        this.mScanningRSSI = -1;
    }

    ADBlePeripheralCallback mBlePeripheralCallback = new ADBlePeripheralCallback() {

        @Override
        public void peripheralDidWriteValueForDescriptor(final ADBlePeripheral peripheral, BluetoothGattDescriptor descriptor, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                if (mNotifyCharacteristic != null) {
                    peripheral.setNotifyValueForCharacteristic(mNotifyCharacteristic, true);
                }
                return;
            } else {
                if (mNotifyCharacteristic != null && mWriteCharacteristic != null) {
                    mSetNotifySuccess = true;
                    if (DCEquipmentManager.getInstance().mCallback != null) {
                        DCEquipmentManager.getInstance().mCallback.equipmentManagerDidConnectEquipment(DCEquipment.this);
                    }
                }
            }
        }

        @Override
        public void peripheralDidWriteValueForCharacteristic(final ADBlePeripheral peripheral, BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void peripheralDidUpdateValueForCharacteristic(final ADBlePeripheral peripheral, final BluetoothGattCharacteristic characteristic, final byte[] value) {
            synchronized (DCEquipment.this) {
                //ADLog.v(TAG, "RCV %s", ADConverter.byteArrayToHexString(value));
                mReceiveData.put(value);
            }
            processReceiveData();
        }

        @Override
        public void peripheralDidReadValueForCharacteristic(final ADBlePeripheral peripheral, BluetoothGattCharacteristic characteristic, int status) {

        }

        @Override
        public void peripheralDidReadRSSI(final ADBlePeripheral peripheral, int rssi, int status) {

        }

        @Override
        public void peripheralDidDiscoverServices(final ADBlePeripheral peripheral, BluetoothGattService[] services, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                peripheral.discoverServices();
                return;
            }
            UUID uuid = null;

            for (BluetoothGattService gattService : services) {
                uuid = gattService.getUuid();

                if (uuid.equals(SERVICE_UUID)) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        uuid = gattCharacteristic.getUuid();

                        int properties = gattCharacteristic.getProperties();

                        if (uuid.equals(NOTIFY_UUID) && (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            ADLog.d(TAG, "%s found NotifyCharacteristic", peripheral.getName());
                            mNotifyCharacteristic = gattCharacteristic;

                        } else if (uuid.equals(WRITE_UUID) && (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                            ADLog.d(TAG, "%s found WriteCharacteristic", peripheral.getName());
                            mWriteCharacteristic = gattCharacteristic;
                        }
                    }
                    break;
                }
            }

            if (mNotifyCharacteristic == null || mWriteCharacteristic == null) {
                DCEquipmentManager.getInstance().cancelEquipmentConnection(DCEquipment.this);
                return;
            }

            if (mNotifyCharacteristic != null) {
                ADLog.d(TAG, "%s setNotifyValueForCharacteristic", peripheral.getName());
                peripheral.setNotifyValueForCharacteristic(mNotifyCharacteristic, true);
            }
        }
    };

    private class ADTimerTask extends TimerTask {
        public void run() {
            invokeGetInfoValue();
        }
    }

    private class GetInclineTask extends TimerTask {

        @Override
        public void run() {
            invokeGetInclineValue();
        }
    }

    void invokeGetInfoValue() {
        if (getConnectionState() == DCEquipmentConnectionStateConnected) {
            if (mErrorNumber == 200) {
                return;
            }

            if (!canGetInfoValue()) {
                return;
            }

            getInfoValue(new DCCommandCompletionBlockWithError() {

                @Override
                public void completed(DCCommand command, DCError error) {
                    if (getConnectionState() == DCEquipmentConnectionStateConnected) {
                        if (error != null && error.getCode() == DCErrorCodeRetryCountExceeded) {
                            setErrorNumber(200);
                        }
                    }
                }
            });
        }
    }

    void invokeGetInclineValue() {
        if (getConnectionState() == DCEquipmentConnectionStateConnected) {
            if (mErrorNumber == 200) {
                return;
            }
        }
    }

    protected boolean canGetInfoValue() {
        if (mIsUpdatingConsole) {
            return false;
        }
        return true;
    }

    abstract void getInfoValue(DCCommandCompletionBlockWithError failure);

    private void sendFirstCommand() {
        if (!mCommands.isEmpty()) {
            DCCommand command;
            synchronized (DCEquipment.this) {
                command = mCommands.peek();
            }
            if (command != null) {
                if (mCurrentCommand == null) {
                    mRetryCount = command.getRetryCount();
                }

                mCurrentCommand = command;

                if (getConnectionState() == DCEquipmentConnectionStateConnected) {
                    if (mPeripheral != null) {
                        byte[] requestData = command.getRequestData();
                        //ADLog.v(TAG, "SND %s", ADConverter.byteArrayToHexString(requestData));
                        mPeripheral.writeValueForCharacteristic(mWriteCharacteristic,
                                requestData, new ADWriteCharacteristicCallback() {

                                    @Override
                                    public void writeCharacteristicFinish() {
                                        ADLog.v(TAG, "writeCharacteristicFinish");
                                        runResendProcess();
                                    }
                                });
                    }
                } else {
                    DCError error =
                            new DCError(DCErrorCodeNotConnected, "Please connect to the equipment");
                    mCurrentCommand.runCompletionBlockWithError(error);
                    removeFirstCommandAndSendNext();
                }
            }
        }
    }


    private void removeFirstCommandAndSendNext() {
        mHandler.removeCallbacks(mResendCommandRunnable);
        mRetryCount = -1;

        mCurrentCommand = null;
        if (!mCommands.isEmpty()) {
            mCommands.poll();
        } else {
            mLastAddCommandClass = null;
        }
        sendFirstCommand();
    }

    Runnable mResendCommandRunnable = new Runnable() {
        @Override
        public void run() {
            resendCommand();
        }
    };

    void runResendProcess() {
        if (mCurrentCommand == null) {
            return;
        }

        long timeout = (long) (mCurrentCommand.getRetryTimeout() * 1000.0f);

        if (timeout > 0) {
            mHandler.postDelayed(mResendCommandRunnable, timeout);
        }
    }

    void resendCommand() {
        if (mCurrentCommand != null) {
            if (mRetryCount > 0) {
                mRetryCount--;
                ADLog.d(TAG, "resendCommand");
                sendFirstCommand();
            } else if (mRetryCount == 0) {
                DCError error = new DCError(DCErrorCodeRetryCountExceeded, "retry count exceeded");
                mCurrentCommand.runCompletionBlockWithError(error);
                ADLog.e(TAG, "retry count exceeded");
                removeFirstCommandAndSendNext();
            }

        }
    }

    boolean canAddCommand(DCCommand command) {
        String commandName = command.getClass().getSimpleName();

        if (mIsUpdatingConsole && !commandName.startsWith("DCUpgradeConsole")) {
            DCError error = new DCError(DCErrorCodeRequestDenied, "Updating console...");
            command.runCompletionBlockWithError(error);

            return false;
        }
        return true;
    }

    void addCommand(DCCommand command) {
        if (getConnectionState() != DCEquipmentConnectionStateConnected) {
            DCError error = new DCError(DCErrorCodeNotConnected, "Please connect to the equipment");
            command.runCompletionBlockWithError(error);
            return;
        }

        if (mInMaintainMenu && !(command instanceof DCGetSaleServiceCommand)) {
            command.runCompletionBlockWithError(new DCError(DCErrorCodeRequestDenied, "In maintain menu..."));
            return;
        }

        if (!canAddCommand(command)) {
            return;
        }

        if ((command.getCompatibilityModes() & mMode) == 0) {
            StringBuilder compatibilityModes = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                int mode = (int) Math.pow(2, i);
                if ((command.getCompatibilityModes() & mode) == mode) {
                    if (compatibilityModes.length() > 0) {
                        compatibilityModes.append(", ");
                    }
                    String correctMode = convertEquipmentModeToString(mode);
                    compatibilityModes.append(correctMode);
                }
            }
            command.runCompletionBlockWithError(new DCError(DCErrorCodeWrongMode, "Please change the mode to %s", compatibilityModes.toString()));
            return;
        }

        DCError error = command.getParameterError();
        if (error != null) {
            command.runCompletionBlockWithError(error);
            return;
        }

        if (command instanceof DCGetInfoValueCommand && mLastAddCommandClass != null && DCGetInfoValueCommand.class.isAssignableFrom(mLastAddCommandClass) && !mCommands.isEmpty()) {
            return;
        }

        mLastAddCommandClass = command.getClass();
        ADLog.d(TAG, "addCommand %s", mLastAddCommandClass.getSimpleName());
        mCommands.add(command);

        if (mCurrentCommand == null) {
            sendFirstCommand();
        }
    }

    private String convertEquipmentModeToString(int mode) {
        String result = null;

        switch (mode) {
            case DCEquipmentModeSetting:
                result = "DCEquipmentModeSetting";
                break;
            case DCEquipmentModeWorkout:
                result = "DCEquipmentModeWorkout";
                break;
        }

        return result;
    }

    private void processReceiveData() {
        synchronized (DCEquipment.this) {
            if (mCurrentCommand == null) {
                return;
            }

            int receiveDataLength = mReceiveData.position();

            // mReceiveData.rewind();
            // byte[] dst = new byte[receiveDataLength];
            // mReceiveData.get(dst, 0, receiveDataLength);
            // ADLog.e(TAG, "Current %s",
            // ADConverter.byteArrayToHexString(dst));

            if (receiveDataLength >= 3) {
                int lastI = -1;
                for (int i = 0; i < receiveDataLength - 1; i++) {
                    byte value = mReceiveData.get(i);
                    byte correctValue = mCurrentCommand.getExpectedResponseFirstValue();
                    if (value == correctValue) {
                        lastI = i;
                        int expectedLength = mCurrentCommand.getExpectedResponseLength();
                        if (expectedLength > 0) {
                            if ((i + expectedLength) <= mReceiveData.position()) {
                                byte[] uncheckedResponse = new byte[expectedLength];
                                mReceiveData.position(i);
                                mReceiveData.get(uncheckedResponse, 0, expectedLength);
                                boolean isExpectedResponse = mCurrentCommand.isExpectedResponse(uncheckedResponse);
                                mReceiveData.position(mReceiveData.limit());
                                mReceiveData.limit(mReceiveData.capacity());
                                if (isExpectedResponse) {
                                    int newPosition = i + expectedLength;
                                    mReceiveData.position(newPosition);
                                    mReceiveData.limit(newPosition);
                                    mReceiveData.compact();

                                    HashMap<String, Object> info = mCurrentCommand.processResponse(uncheckedResponse);
                                    mCurrentCommand.runCompletionBlock(info);
                                    removeFirstCommandAndSendNext();
                                    break;
                                }
                            }
                        } else {
                            boolean ifBreak = false;
                            for (expectedLength = 3; (i + expectedLength) <= mReceiveData.position(); expectedLength++) {
                                mReceiveData.flip();
                                byte[] uncheckedResponse = new byte[expectedLength];
                                mReceiveData.get(uncheckedResponse, i, expectedLength);
                                boolean isExpectedResponse = mCurrentCommand.isExpectedResponse(uncheckedResponse);
                                mReceiveData.position(mReceiveData.limit());
                                mReceiveData.limit(mReceiveData.capacity());
                                if (isExpectedResponse) {
                                    int newPosition = i + expectedLength + mReceiveData.position() - (i + expectedLength);
                                    mReceiveData.position(newPosition);
                                    mReceiveData.limit(newPosition);
                                    mReceiveData.compact();

                                    HashMap<String, Object> info = mCurrentCommand.processResponse(uncheckedResponse);
                                    mCurrentCommand.runCompletionBlock(info);
                                    removeFirstCommandAndSendNext();
                                    ifBreak = true;
                                    break;
                                }
                            }
                            if (ifBreak) {
                                break;
                            }
                        }
                    }
                }

                if (mCurrentCommand != null) {
                    int expectedLength = mCurrentCommand.getExpectedResponseLength();
                    if (lastI == -1) {
                        mReceiveData.clear();
                    } else {
                        if (expectedLength > 0 && (lastI + expectedLength) <= mReceiveData.position()) {
                            int position = lastI + expectedLength;
                            mReceiveData.position(position);
                            mReceiveData.limit(receiveDataLength);
                            mReceiveData.compact();

                            // print
                            // position = mReceiveData.position();
                            // mReceiveData.rewind();
                            // dst = new byte[position];
                            // mReceiveData.get(dst, 0, position);
                            // ADLog.e(TAG, "lastI %d After %s",
                            // lastI,ADConverter.byteArrayToHexString(dst));
                        }
                    }
                }
            }
        }
    }

    public abstract void setMode(int mode, DCCompletionBlock success, DCCompletionBlockWithError failure);

    public void setSessionData(int sessionDataType, final DCCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCSetSessionDataCommand setSessionDataCommand = new DCSetSessionDataCommand();
        setSessionDataCommand.mSessionData = (byte) sessionDataType;
        setSessionDataCommand.setCompletionBlock(new DCCommandCompletionBlock() {

            @Override
            public void completed(DCCommand command) {
                if (success != null) {
                    success.completed(DCEquipment.this);
                }

            }
        });

        setSessionDataCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }

            }
        });

        addCommand(setSessionDataCommand);
    }

    public void maintainMenuOpen(boolean openMenu, final DCCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCSetSaleServiceCommand setSaleServiceCommand = new DCSetSaleServiceCommand();
        setSaleServiceCommand.mSaleService = (byte) (openMenu ? 1 : 0);
        setSaleServiceCommand.setCompletionBlock(new DCCommandCompletionBlock() {

            @Override
            public void completed(DCCommand command) {
                mInMaintainMenu = true;

                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        DCEquipment.this.getMaintainMenuState(new DCGetMaintainMenuStateCompletionBlock() {

                            @Override
                            public void completed(DCEquipment equipment, boolean inMaintainMenu) {
                                mInMaintainMenu = inMaintainMenu;
                                if (!mInMaintainMenu) {
                                    timer.cancel();
                                    if (success != null) {
                                        success.completed(DCEquipment.this);
                                    }
                                }
                            }
                        }, new DCCompletionBlockWithError() {

                            @Override
                            public void completedWithError(DCEquipment equipment, DCError error) {

                            }
                        });
                    }
                }, 1000, 2000);
            }
        });

        setSaleServiceCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }

            }
        });

        addCommand(setSaleServiceCommand);
    }

    public interface DCGetMaintainMenuStateCompletionBlock {
        void completed(DCEquipment equipment, boolean inMaintainMenu);
    }

    public void getMaintainMenuState(final DCGetMaintainMenuStateCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCGetSaleServiceCommand getSaleServiceCommand = new DCGetSaleServiceCommand();

        getSaleServiceCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo() {

            @Override
            public void completed(DCCommand command, HashMap<String, Object> info) {
                Integer saleService = (Integer) info.get("saleService");
                boolean inMaintainMenu = (saleService != 0);
                if (success != null) {
                    success.completed(DCEquipment.this, inMaintainMenu);
                }

            }
        });

        getSaleServiceCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }
            }
        });

        addCommand(getSaleServiceCommand);
    }

    public void enterMaintainMenu(DCCompletionBlock success, DCCompletionBlockWithError failure) {
        maintainMenuOpen(true, success, failure);
    }

    public void rebootConsole(final DCCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCRebootConsoleCommand rebootConsoleCommand = new DCRebootConsoleCommand();
        rebootConsoleCommand.setCompletionBlock(new DCCommandCompletionBlock() {

            @Override
            public void completed(DCCommand command) {
                if (success != null) {
                    success.completed(DCEquipment.this);
                }

            }
        });

        rebootConsoleCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }

            }
        });

        addCommand(rebootConsoleCommand);
    }

    public void setHotKey(int hotKey, final DCCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCSetHotKeyCommand setHotKeyPauseResumeCommand = new DCSetHotKeyCommand();
        setHotKeyPauseResumeCommand.mHotKey = (byte) hotKey;

        setHotKeyPauseResumeCommand.setCompletionBlock(new DCCommandCompletionBlock() {

            @Override
            public void completed(DCCommand command) {
                if (success != null) {
                    success.completed(DCEquipment.this);
                }

            }
        });

        setHotKeyPauseResumeCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }

            }
        });

        addCommand(setHotKeyPauseResumeCommand);
    }

    public void setBeep(boolean on, final DCCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCSetBeepCommand setBeepCommand = new DCSetBeepCommand();
        setBeepCommand.mBeepOn = (byte) (on ? 1 : 0);

        setBeepCommand.setCompletionBlock(new DCCommandCompletionBlock() {

            @Override
            public void completed(DCCommand command) {
                if (success != null) {
                    success.completed(DCEquipment.this);
                }

            }
        });

        setBeepCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }

            }
        });

        addCommand(setBeepCommand);
    }

    public void setFanSpeedLevel(int fanSpeedLevel, final DCCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCSetFanSpeedLevelCommand setFanSpeedLevelCommand = new DCSetFanSpeedLevelCommand();
        setFanSpeedLevelCommand.mFanSpeedLevel = (byte) fanSpeedLevel;

        setFanSpeedLevelCommand.setCompletionBlock(new DCCommandCompletionBlock() {

            @Override
            public void completed(DCCommand command) {
                if (success != null) {
                    success.completed(DCEquipment.this);
                }

            }
        });

        setFanSpeedLevelCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }

            }
        });

        addCommand(setFanSpeedLevelCommand);
    }

    public interface DCGetEquipmentIDCompletionBlock {
        void completed(DCEquipment equipment, String equipmentID);
    }

    public void getEquipmentID(final DCGetEquipmentIDCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCGetEquipmentIDCommand getEquipmentIDCommand = new DCGetEquipmentIDCommand();

        getEquipmentIDCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo() {

            @Override
            public void completed(DCCommand command, HashMap<String, Object> info) {
                String equipmentID = (String) info.get("equipmentID");
                if (success != null) {
                    success.completed(DCEquipment.this, equipmentID);
                }

            }
        });

        getEquipmentIDCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }
            }
        });

        addCommand(getEquipmentIDCommand);
    }

    public interface DCGetErrorHistoricListCompletionBlock {
        void completed(DCEquipment equipment, int[] errorHistoricList);
    }

    public void getErrorHistoricList(final DCGetErrorHistoricListCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCGetErrorHistoricListCommand getErrorHistoricListCommand = new DCGetErrorHistoricListCommand();

        getErrorHistoricListCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo() {

            @Override
            public void completed(DCCommand command, HashMap<String, Object> info) {
                int[] errorHistoricList = (int[]) info.get("errorHistoricList");
                if (success != null) {
                    success.completed(DCEquipment.this, errorHistoricList);
                }

            }
        });

        getErrorHistoricListCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }
            }
        });

        addCommand(getErrorHistoricListCommand);
    }

    /**
     * @param user    the user number(id) in the console.
     * @param success If success, api will run this block.
     * @param failure If failed, api will run this block.
     * @brief reset console workout result
     * @details This is setting & workout mode.
     */
    public void resetConsoleWorkoutResult(int user, final @NonNull DCCompletionBlock success, final @NonNull DCCompletionBlockWithError failure) {
        DCResetWorkoutResultCommand resetWorkoutResultCommand = new DCResetWorkoutResultCommand();
        resetWorkoutResultCommand.setUser(user);

        resetWorkoutResultCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo() {

            @Override
            public void completed(DCCommand command, HashMap<String, Object> info) {

                if (success != null) {
                    success.completed(DCEquipment.this);
                }

            }
        });

        resetWorkoutResultCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, error);
                }
            }
        });

        addCommand(resetWorkoutResultCommand);
    }

    public interface DCEquipmentUpgradeConsoleUpdateProgressBlock {
        void onProgressChanged(float progress);
    }

    public void upgradeConsole(final File file, final DCCompletionBlock success, final DCEquipmentUpgradeConsoleUpdateProgressBlock updateProgressBlock, final DCCompletionBlockWithError failure) {
        if (mIsUpdatingConsole) {
            if (failure != null) {
                DCError error = new DCError(DCEquipment.DCErrorCodeRequestDenied, "Updating console...");
                failure.completedWithError(DCEquipment.this, error);
            }

            return;
        }

        rebootConsole(null, null);

        mIsUpdatingConsole = true;
        FileInputStream fileInputStream;
        BufferedReader bufferReader;
        if (file.exists()) {
            try {
                fileInputStream = new FileInputStream(file);
                bufferReader = new BufferedReader(new InputStreamReader(fileInputStream));
                String line;
                final StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\r\n");
                }

                bufferReader.close();

                String fileName = file.getName();
                int index = fileName.lastIndexOf(".");
                if (index > 0) {
                    fileName = fileName.substring(0, index);
                }
                String md5 = md5(stringBuilder.toString());

                boolean incorrectMd5 = false;
                if (fileName.length() >= 32) {
                    String fileNameMd5 = fileName.substring(fileName.length() - 32);
                    if (!fileNameMd5.equalsIgnoreCase(md5)) {
                        incorrectMd5 = true;
                    }
                } else {
                    incorrectMd5 = true;
                }

                if (incorrectMd5) {
                    if (failure != null) {
                        DCError error = new DCError(DCErrorCodeUpgradeConsoleIncorrectFile, "upgrade console error: incorrect file");
                        failure.completedWithError(DCEquipment.this, error);
                    }
                    mIsUpdatingConsole = false;
                    return;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                DCUpgradeConsoleCommand upgradeConsoleCommand = new DCUpgradeConsoleCommand();

                upgradeConsoleCommand.setCompletionBlock(new DCCommandCompletionBlock() {

                    @Override
                    public void completed(DCCommand command) {
                        DCUpgradeConsoleGetIDCommand upgradeConsoleGetIDCommand = new DCUpgradeConsoleGetIDCommand();

                        upgradeConsoleGetIDCommand.setCompletionBlock(new DCCommandCompletionBlock() {

                            @Override
                            public void completed(DCCommand command) {
                                DCUpgradeConsoleGetVersionCommand upgradeConsoleGetVersionCommand = new DCUpgradeConsoleGetVersionCommand();
                                upgradeConsoleGetVersionCommand.setCompletionBlock(new DCCommandCompletionBlock() {

                                    @Override
                                    public void completed(DCCommand command) {
                                        DCUpgradeConsoleGetResetVectorCommand upgradeConsoleGetResetVectorCommand = new DCUpgradeConsoleGetResetVectorCommand();
                                        upgradeConsoleGetResetVectorCommand.setCompletionBlock(new DCCommandCompletionBlock() {

                                            @Override
                                            public void completed(DCCommand command) {
                                                DCUpgradeConsoleEraseCommand upgradeConsoleEraseCommand = new DCUpgradeConsoleEraseCommand();
                                                upgradeConsoleEraseCommand.setCompletionBlock(new DCCommandCompletionBlock() {

                                                    @Override
                                                    public void completed(DCCommand command) {
                                                        programPicxx(stringBuilder.toString(), success, updateProgressBlock, failure);
                                                    }
                                                });

                                                upgradeConsoleEraseCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

                                                    @Override
                                                    public void completed(DCCommand command, DCError error) {
                                                        if (failure != null) {
                                                            failure.completedWithError(DCEquipment.this, new DCError(DCErrorCodeUpgradeConsoleEraseTimeout, "upgrade console error: erase timeout"));
                                                        }
                                                        mIsUpdatingConsole = false;

                                                    }
                                                });

                                                addCommand(upgradeConsoleEraseCommand);

                                            }
                                        });

                                        upgradeConsoleGetResetVectorCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

                                            @Override
                                            public void completed(DCCommand command, DCError error) {
                                                if (failure != null) {
                                                    failure.completedWithError(DCEquipment.this, new DCError(DCErrorCodeUpgradeConsoleResetVectorReadTimeout, "upgrade console error: reset vector read timeout"));
                                                }
                                                mIsUpdatingConsole = false;

                                            }
                                        });

                                        addCommand(upgradeConsoleGetResetVectorCommand);

                                    }
                                });

                                upgradeConsoleGetVersionCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

                                    @Override
                                    public void completed(DCCommand command, DCError error) {
                                        if (failure != null) {
                                            failure.completedWithError(DCEquipment.this, new DCError(DCErrorCodeUpgradeConsolePICsVersionTimeout, "upgrade console error: PICs Version timeout"));
                                        }
                                        mIsUpdatingConsole = false;

                                    }
                                });

                                addCommand(upgradeConsoleGetVersionCommand);

                            }
                        });

                        upgradeConsoleGetIDCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

                            @Override
                            public void completed(DCCommand command, DCError error) {
                                if (failure != null) {
                                    failure.completedWithError(DCEquipment.this, new DCError(DCErrorCodeUpgradeConsoleConnectTimeout, "upgrade console error: connect timeout"));
                                }
                                mIsUpdatingConsole = false;

                            }
                        });

                        addCommand(upgradeConsoleGetIDCommand);

                    }
                });

                upgradeConsoleCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

                    @Override
                    public void completed(DCCommand command, DCError error) {
                        if (failure != null) {
                            failure.completedWithError(DCEquipment.this, error);
                        }
                        mIsUpdatingConsole = false;
                    }
                });

                addCommand(upgradeConsoleCommand);

            } catch (IOException e) {
                e.printStackTrace();
                if (failure != null) {
                    DCError error = new DCError(DCErrorCodeUpgradeConsoleIncorrectFile, "upgrade console error: %s", e.getLocalizedMessage());
                    failure.completedWithError(DCEquipment.this, error);
                }
                mIsUpdatingConsole = false;
            }

        } else {
            if (failure != null) {
                DCError error = new DCError(DCErrorCodeUpgradeConsoleIncorrectFile, "upgrade console error: file does not exists");
                failure.completedWithError(DCEquipment.this, error);
            }
            mIsUpdatingConsole = false;
        }

    }

    private void programPicxx(String content, DCCompletionBlock success, final DCEquipmentUpgradeConsoleUpdateProgressBlock updateProgressBlock, DCCompletionBlockWithError failure) {

        content = content.replaceAll(" ", "");
        String[] contentRows = content.split("[\\r\\n]+");


        int length = contentRows.length;
        int blockCount = length / 16;

        if (length % 16 == 0 && blockCount > 0) {
            programPicxx(contentRows, blockCount, 0, success, updateProgressBlock, failure);
        } else {
            if (failure != null) {
                failure.completedWithError(DCEquipment.this, new DCError(DCErrorCodeUpgradeConsoleIncorrectFile, "upgrade console error: incorrect file"));
            }
            mIsUpdatingConsole = false;
        }
    }

    private void programPicxx(final String[] contentRows, final int blockCount, final int currentBlockIndex, final DCCompletionBlock success, final DCEquipmentUpgradeConsoleUpdateProgressBlock updateProgressBlock, final DCCompletionBlockWithError failure) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(261);
        byteBuffer.put((byte) 0x02);
        byteBuffer.put((byte) 0x01);


        for (int i = 16 * currentBlockIndex; i < 16 * (currentBlockIndex + 1); i++) {
            String contentRow = contentRows[i];
            if (i == 16 * currentBlockIndex) {
                String addressString = contentRow.substring(0, 6);
                Integer address = Integer.parseInt(addressString, 16) / 2;
                byte[] addressData = new byte[]{
                        (byte) (address & 0xFF),
                        (byte) ((address >> 8) & 0xFF),
                        (byte) ((address >> 16) & 0xFF)
                };
                byteBuffer.put(addressData);
            }
            String rowDataString = contentRow.substring(6);
            byte[] rowData = ADConverter.hexStringToByteArray(rowDataString);
            byteBuffer.put(rowData);
        }

        byteBuffer.rewind();
        byte[] commandData = byteBuffer.array();

        DCUpgradeConsoleProgramPicxxCommand upgradeConsoleProgramPicxxCommand = new DCUpgradeConsoleProgramPicxxCommand(commandData);

        upgradeConsoleProgramPicxxCommand.setCompletionBlock(new DCCommandCompletionBlock() {
            @Override
            public void completed(DCCommand command) {

                if (updateProgressBlock != null) {
                    updateProgressBlock.onProgressChanged((float) (currentBlockIndex + 1) * 100.0f / blockCount);
                }
                int newBlockIndex = currentBlockIndex + 1;
                if (newBlockIndex < blockCount) {
                    programPicxx(contentRows, blockCount, newBlockIndex, success, updateProgressBlock, failure);
                } else {
                    DCUpgradeConsoleProgramFinishCommand programFinishCommand = new DCUpgradeConsoleProgramFinishCommand();
                    programFinishCommand.setCompletionBlock(new DCCommandCompletionBlock() {

                        @Override
                        public void completed(DCCommand command) {
                            int errorNumber = getErrorNumber();
                            if (errorNumber == 200) {
                                setErrorNumber(255);
                            }

                            if (success != null) {
                                success.completed(DCEquipment.this);
                            }
                            mIsUpdatingConsole = false;

                        }
                    });

                    programFinishCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

                        @Override
                        public void completed(DCCommand command, DCError error) {
                            if (failure != null) {
                                failure.completedWithError(DCEquipment.this, new DCError(DCErrorCodeUpgradeConsoleCMDFinishTimeout, "upgrade console error: command finish"));
                            }
                            mIsUpdatingConsole = false;

                        }
                    });

                    addCommand(programFinishCommand);

                }
            }
        });

        upgradeConsoleProgramPicxxCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {

            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null) {
                    failure.completedWithError(DCEquipment.this, new DCError(DCErrorCodeUpgradeConsoleFlashWriting, "upgrade console error: flash writing"));
                }

            }
        });

        addCommand(upgradeConsoleProgramPicxxCommand);
    }

    public void setDisplayZonesSecondArea(DCEquipmentDisplayZoneSecondAreaParameters displayZoneParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCSetDisplayZoneSecondAreaCommand setDisplayZoneCommand = new DCSetDisplayZoneSecondAreaCommand();

        setDisplayZoneCommand.displayZone7Parameter = displayZoneParameters.getDisplayZone7Parameter();
        setDisplayZoneCommand.displayZone8Parameter = displayZoneParameters.getDisplayZone8Parameter();
        setDisplayZoneCommand.displayZone9Parameter = displayZoneParameters.getDisplayZone9Parameter();
        setDisplayZoneCommand.displayZone10Parameter = displayZoneParameters.getDisplayZone10Parameter();
        setDisplayZoneCommand.displayZone11Parameter = displayZoneParameters.getDisplayZone11Parameter();
        setDisplayZoneCommand.displayZone12Parameter = displayZoneParameters.getDisplayZone12Parameter();

        setDisplayZoneCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo() {
            @Override
            public void completed(DCCommand command, HashMap<String, Object> info) {
                if (success != null)
                    success.completed(DCEquipment.this);
            }
        });

        setDisplayZoneCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {
            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null)
                    failure.completedWithError(DCEquipment.this, error);
            }
        });

        addCommand(setDisplayZoneCommand);
    }

    public void setDisplayZoneOffSecondArea(DCEquipmentDisplayZoneOffSecondAreaParameters displayZoneOffParameters, final DCCompletionBlock success, final DCCompletionBlockWithError failure) {
        DCSetDisplayZoneOffSecondAreaCommand setDisplayZoneOffCommand = new DCSetDisplayZoneOffSecondAreaCommand();

        setDisplayZoneOffCommand.displayZone7OffParameter = displayZoneOffParameters.getDisplayZone7Off();
        setDisplayZoneOffCommand.displayZone8OffParameter = displayZoneOffParameters.getDisplayZone8Off();
        setDisplayZoneOffCommand.displayZone9OffParameter = displayZoneOffParameters.getDisplayZone9Off();
        setDisplayZoneOffCommand.displayZone10OffParameter = displayZoneOffParameters.getDisplayZone10Off();
        setDisplayZoneOffCommand.displayZone11OffParameter = displayZoneOffParameters.getDisplayZone11Off();
        setDisplayZoneOffCommand.displayZone12OffParameter = displayZoneOffParameters.getDisplayZone12Off();

        setDisplayZoneOffCommand.setCompletionBlockWithInfo(new DCCommandCompletionBlockWithInfo() {
            @Override
            public void completed(DCCommand command, HashMap<String, Object> info) {
                if (success != null)
                    success.completed(DCEquipment.this);
            }
        });

        setDisplayZoneOffCommand.setCompletionBlockWithError(new DCCommandCompletionBlockWithError() {
            @Override
            public void completed(DCCommand command, DCError error) {
                if (failure != null)
                    failure.completedWithError(DCEquipment.this, error);
            }
        });

        addCommand(setDisplayZoneOffCommand);
    }

    private String md5(String input) {
        String result = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(input.getBytes());
            result = ADConverter.byteArrayToHexString(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
