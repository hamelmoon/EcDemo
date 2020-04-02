package com.sh.ec.service;


import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.appdevice.domyos.DCBike;
import com.appdevice.domyos.DCCompletionBlockWithError;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentInfo;
import com.appdevice.domyos.DCEquipmentManager;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.DCTreadmill;
import com.sh.ec.bluetooth.manager.BluetoothManager;
import com.sh.ec.bluetooth.manager.connection.BluetoothConnectionManager;
import com.sh.ec.event.EquipmentEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//处理蓝牙
public class SportDataService extends Service {

    private String TAG = "SportDataService";
    private DCEquipment mConnectedEquipment = null;
    private static DCEquipment mEquipment = null;
    private boolean isEquipmentConnected = false;
    List<DCEquipment> equipList = new ArrayList<>();
    int reconnectCount = 0;
    private static BluetoothConnectionManager bluetoothConnectionManager;
    private static BluetoothManager manager;
    public static int equipmentId;
    public static float equipmentVersion;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        initListener();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initListener() {
        DCEquipmentManager.getInstance().setCallBack(new DCEquipmentManager.DCEquipmentManagerCallback() {
            @Override
            public void equipmentManagerDidUnBound() {
                Log.e(TAG, "CallBack-------equipmentManagerDidUnBound");
            }

            @Override
            public void equipmentManagerDidInitialized() {
                Log.e(TAG, "CallBack-------equipmentManagerDidInitialized-====" + DCEquipmentManager.getInstance().getInitializationState());
                if (DCEquipmentManager.getInstance().getInitializationState()) {
                    DCEquipmentManager.getInstance().scanEquipments();
                } else {
                    DCEquipmentManager.getInstance().scanEquipments();
                }
            }

            @Override
            public void equipmentManagerDidDiscoverEquipment(DCEquipment equipment) {
                Log.e(TAG, "CallBack-------equipmentManagerDidDiscoverEquipment");

                DCEquipment[] equipments = new DCEquipment[DCEquipmentManager.getInstance().getEquipments().size()];
                equipList = new ArrayList<>(DCEquipmentManager.getInstance().getEquipments());

                Collections.sort(equipList, new Comparator<DCEquipment>() {
                    @Override
                    public int compare(DCEquipment o1, DCEquipment o2) {
                        return o2.getScanningRSSI() - o1.getScanningRSSI();
                    }
                });
                EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_EQUIPMENT_SEARCH, equipList));
            }

            @Override
            public void equipmentManagerDidDisconnectEquipment(DCEquipment equipment) {
                Log.e(TAG, "CallBack-------equipmentManagerDidDisconnectEquipment");
                isEquipmentConnected = false;
                equipList.clear();
                EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_EQUIPMENT_DISCONNECT));

                if (reconnectCount <= 2 && mConnectedEquipment != null) {
                    Log.d(TAG, "reconnect equipment: " + mConnectedEquipment.getName());
                    DCEquipmentManager.getInstance().connectEquipment(mConnectedEquipment);
                } else {
                    DCEquipmentManager.getInstance().stopScanEquipments();
                }
            }


            @Override
            public void equipmentManagerDidConnectEquipment(DCEquipment equipment) {
                Log.e(TAG, "CallBack-------equipmentManagerDidConnectEquipment");
                DCEquipmentManager.getInstance().stopScanEquipments();
                mConnectedEquipment = null;
                isEquipmentConnected = true;
                mEquipment = equipment;

                DCBike treadmill = (DCBike) equipment;

                treadmill.getEquipmentID(new DCEquipment.DCGetEquipmentIDCompletionBlock() {
                    @Override
                    public void completed(DCEquipment equipment, String ID) {
                        Log.e("equipmentID", "-------------" + ID);
                        equipmentId = Integer.valueOf(ID);
                    }
                }, new DCCompletionBlockWithError() {
                    @Override
                    public void completedWithError(DCEquipment equipment, DCError error) {
                        Log.e("equipmentID", "-------------" + error.getDescription());

                    }
                });

                treadmill.getEquipmentInfo(new DCBike.DCBikeGetEquipmentInfoCompletionBlock() {
                    @Override
                    public void completed(DCEquipment equipment, DCEquipmentInfo consoleEquipmentInfo) {
                        equipmentVersion = consoleEquipmentInfo.getFirmwareVersion();
                        EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_EQUIPMENT_CONNECTED, equipment));

                    }
                }, new DCCompletionBlockWithError() {
                    @Override
                    public void completedWithError(DCEquipment equipment, DCError error) {

                    }
                });


                bluetoothConnectionManager = new BluetoothConnectionManager(DCEquipmentManager.getInstance(), SportDataService.this);
                manager = new BluetoothManager(bluetoothConnectionManager);
                manager.initializeSpecificEquipmentManager(mEquipment, true);

            }
        });
    }

    //初始化
    public void scan(Context context) {
        reconnectCount = 0;
        if (mConnectedEquipment != null) {
            DCEquipmentManager.getInstance().cancelEquipmentConnection(mConnectedEquipment);
        }
        if (DCEquipmentManager.getInstance().getInitializationState()) {
            if (mEquipment != null && mEquipment.getConnectionState() != DCEquipment.DCEquipmentConnectionStateDisconnected) {
                mConnectedEquipment = null;
                DCEquipmentManager.getInstance().cancelEquipmentConnection(mEquipment);
            } else if (DCEquipmentManager.getInstance().isScanning()) {
                DCEquipmentManager.getInstance().stopScanEquipments();
            } else {
                equipList.clear();
                DCEquipmentManager.getInstance().scanEquipments();
            }
        } else {
            DCEquipmentManager.getInstance().initialize(context);

        }
    }


    public void stopScan() {
        if (DCEquipmentManager.getInstance().isScanning()) {
            DCEquipmentManager.getInstance().stopScanEquipments();
        }
    }


    public void connectEquipment(DCEquipment mEquipment) {
        DCEquipmentManager.getInstance().connectEquipment(mEquipment);

    }

    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }

    public static DCEquipment getEquipment() {
        if (mEquipment != null) {
            return mEquipment;
        }
        return null;
    }

    public void getStart() {
        if (manager == null) {
            Log.e(TAG, "------manager==null-------");
            return;
        }
        manager.startProgram();

    }

    public void getPause() {
        if (manager == null) {

            return;
        }
        manager.pause();
    }

    public void getStop() {
        if (manager == null) {
            Log.e(TAG, "------manager==null-------");

            return;
        }
        manager.stopProgram();


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void catchEvent(EquipmentEvent event) {
        switch (event.action) {
            case EquipmentEvent.ACTION_QUICK_START:
                getStart();
                //  Log.e(TAG, "------ACTION_QUICK_START-------"+  manager.getEquipmentInfo());
                break;
            case EquipmentEvent.ACTION_PROGRAM_START:
                break;
            case EquipmentEvent.ACTION_PAUSE:
                getPause();
                Log.e(TAG, "------ACTION_PAUSE-------");

                break;
            case EquipmentEvent.ACTION_STOP:
                Log.e(TAG, "------ACTION_STOP-------");

                getStop();
                break;
            case EquipmentEvent.ACTION_RESTART:
                // manager.stopProgram();
                manager.setSpeedCmd(event.last_speed);
                manager.setResistance(event.last_incline);
                manager.startProgram();


                break;
        }

    }

    @Override
    public void onDestroy() {
        isEquipmentConnected = false;
        stopScan();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    public void cancelEquipment() {
        if (BluetoothManager.isBluetoothPhoneEnabled()
                && DCEquipmentManager.getInstance().getInitializationState()) {
            DCEquipmentManager.getInstance().cancelEquipmentConnection(mEquipment);

        }
    }
}
