package com.sh.ec.service;


import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.appdevice.domyos.DCCompletionBlockWithError;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentManager;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.DCTreadmill;
import com.sh.ec.bluetooth.manager.BluetoothManager;
import com.sh.ec.bluetooth.manager.BluetoothManagerDemo;
import com.sh.ec.bluetooth.manager.ManagerEventListener;
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
public class SportDataServiceDemo extends Service{

    private String TAG = "SportDataServiceDemo";

    ManagerEventListener managerEventListener;

    private static BluetoothConnectionManager bluetoothConnectionManager;
    private static BluetoothManagerDemo manager;

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

        bluetoothConnectionManager = new BluetoothConnectionManager(DCEquipmentManager.getInstance(), SportDataServiceDemo.this);
        manager = new BluetoothManagerDemo(bluetoothConnectionManager);
        Log.e("DEMO","------------------");
    }

    //初始化
    public void scan(Context context) {
        bluetoothConnectionManager = new BluetoothConnectionManager(DCEquipmentManager.getInstance(), this.getApplicationContext());
        manager = new BluetoothManagerDemo(bluetoothConnectionManager);
        Log.e("DEMO","------------------");
        manager.updateEquipmentList();
    }




    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
