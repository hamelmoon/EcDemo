package com.sh.ec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.appdevice.domyos.DCBike;
import com.appdevice.domyos.DCBikeSportData;
import com.appdevice.domyos.DCCompletionBlock;
import com.appdevice.domyos.DCCompletionBlockWithError;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentInfo;
import com.appdevice.domyos.DCEquipmentManager;
import com.appdevice.domyos.DCError;
import com.appdevice.domyos.DCTreadmill;
import com.appdevice.domyos.parameters.treadmill.DCTreadmillWorkoutModeSetInfoParameters;
import com.sh.ec.adapter.MachineListAdapter;
import com.sh.ec.adapter.MachineListDemoAdapter;
import com.sh.ec.bluetooth.BluetoothConnectionState;
import com.sh.ec.bluetooth.manager.BluetoothManager;
import com.sh.ec.bluetooth.manager.connection.BluetoothConnectionManager;
import com.sh.ec.bluetooth.manager.utils.BluetoothEquipmentConsoleUtils;
import com.sh.ec.bluetooth.manager.utils.DCUnit;
import com.sh.ec.bluetooth.manager.utils.TypeConstants;
import com.sh.ec.entity.ClickEntity;
import com.sh.ec.event.EquipmentEvent;
import com.sh.ec.service.MangerService;
import com.sh.ec.service.SportDataService;
import com.sh.ec.utils.LogUtils;
import com.sh.ec.utils.SharedPreferenceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static com.sh.ec.bluetooth.manager.BluetoothManager.EQUIPMENT_WORKOUT_ERROR_CODE;
import static com.sh.ec.bluetooth.manager.BluetoothManager.isConnected;
import static com.sh.ec.bluetooth.manager.utils.DCUnit.CURRENT_ROTATION;
import static com.sh.ec.entity.PauseCauseEnum.SESSION_STOP;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private String TAG = "MainActivity";
    List<DCEquipment> equipList = new ArrayList<>();
    SportDataService service;
    DCEquipment equipment;
    Intent intent;
    private MachineListAdapter machineListAdapter;

    private final DCCompletionBlockWithError genericErrorBlock = new DCCompletionBlockWithError() {
        @Override
        public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
            Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
                    dcEquipment.getName(), dcError.getDescription());

        }

        private final DCCompletionBlockWithError idErrorBlock = new DCCompletionBlockWithError() {
            @Override
            public void completedWithError(DCEquipment dcEquipment, DCError dcError) {
                Timber.e("GENERIC ERROR %s - %s: %s", String.valueOf(dcError.getCode()),
                        dcEquipment.getName(), dcError.getDescription());

            }
        };

    };


    /**
     * Used to trigger equipment pause after session stop and data cleared
     */
    private final DCCompletionBlock sessionStopCompletionBlock = dcEquipment -> {
        LogUtils.e("BLUETOOTH MANAGER CLEAR SESSION DATA SESSION CLEAR ------> SUCCESS... ASKING SESSION PAUSE...");
    };

    /**
     * Used to showView in console
     * <p>
     * TODO !!
     */
    private final DCCompletionBlock genericCompletionBlock = dcEquipment -> {
        //empty
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        recyclerView = findViewById(R.id.device_recycler);
        intent = new Intent(this, SportDataService.class);
        startService(intent);
        service = new SportDataService();

        service.scan(this);

    }


    DCBike bike;
    DCBikeSportData sportData;

    /**
     * 设备连接情况
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void equipmentEvent(EquipmentEvent event) {
        switch (event.action) {
            case EquipmentEvent.ACTION_EQUIPMENT_CONNECTED:
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageDrawable(getDrawable(R.mipmap.icon_bluetooth_on));
                //machineListAdapter.notifyDataSetChanged();
                SharedPreferenceUtils.put(this, "isConnected", event.ewEquipment.getName());
                //service.startProgram();
                bike = (DCBike) event.ewEquipment;
                Log.e(TAG, "ACTION_EQUIPMENT_CONNECTED===TabOnEquipment=" + bike.getTabOnEquipment());

                bike.setListener(new DCBike.DCBikeListener() {
                    @Override
                    public void equipmentTabOnEquipmentChanged(DCEquipment equipment, boolean tabOnEquipment) {
                        Log.e(TAG, "equipmentTabOnEquipmentChanged===" + tabOnEquipment);
                    }

                    @Override
                    public void equipmentErrorOccurred(DCEquipment equipment, int errorNumber) {
                        Log.e(TAG, "equipmentErrorOccurred===" + errorNumber);

                    }

                    @Override
                    public void equipmentPressedButtonChanged(DCEquipment equipment, int pressedButton) {
                        Log.e(TAG, "equipmentPressedButtonChanged===" + pressedButton);

                    }

                    @Override
                    public void equipmentOnHotKeyStatusChanged(DCEquipment equipment, int hotKeyStatus) {
                        Log.e(TAG, "equipmentOnHotKeyStatusChanged===" + hotKeyStatus);

                    }

                    @Override
                    public void equipmentOnFanSpeedLevelChanged(DCEquipment equipment, int fanSpeedLevel) {
                        Log.e(TAG, "equipmentOnFanSpeedLevelChanged===" + fanSpeedLevel);

                    }
                });

                sportData = bike.getSportData();
                sportData.setListener(new DCBikeSportData.DCBikeSportDataListener() {
                    @Override
                    public void onWattChanged(float watt) {
                        Log.e(TAG, "onWattChanged===" + watt);

                    }

                    @Override
                    public void onCurrentRPMChanged(int currentRPM) {
                        Log.e(TAG, "onCurrentRPMChanged===" + currentRPM);

                    }

                    @Override
                    public void onTorqueResistanceLevelChanged(int torqueResistanceLevel) {
                        Log.e(TAG, "onTorqueResistanceLevelChanged===" + torqueResistanceLevel);

                    }

                    @Override
                    public void onCurrentSpeedKmPerHourChanged(float currentSpeedKmPerHour) {
                        Log.e(TAG, "onCurrentSpeedKmPerHourChanged===" + currentSpeedKmPerHour);

                    }

                    @Override
                    public void onCurrentSessionCumulativeKCalChanged(int currentSessionCumulativeKCal) {
                        Log.e(TAG, "onCurrentSessionCumulativeKCalChanged===" + currentSessionCumulativeKCal);

                    }

                    @Override
                    public void onCurrentSessionCumulativeKMChanged(float currentSessionCumulativeKM) {
                        Log.e(TAG, "onCurrentSessionCumulativeKMChanged===" + currentSessionCumulativeKM);

                    }

                    @Override
                    public void onAnalogHeartRateChanged(int analogHeartRate) {
                        Log.e(TAG, "onAnalogHeartRateChanged===" + analogHeartRate);

                    }

                    @Override
                    public void onCurrentSessionAverageSpeedChanged(float currentSessionAverageSpeed) {
                        Log.e(TAG, "onCurrentSessionAverageSpeedChanged===" + currentSessionAverageSpeed);

                    }

                    @Override
                    public void onCountChanged(int count) {

                    }
                });
                bike.setMode(2, sessionStopCompletionBlock, genericErrorBlock);

                bike.setHotKey(1, sessionStopCompletionBlock, genericErrorBlock);
                bike.setFanSpeedLevel(2, genericCompletionBlock, genericErrorBlock);
                BluetoothEquipmentConsoleUtils.displayZoneInformation(TypeConstants.TYPE_SPORT_BIKE, bike,
                        3200001, 1, CURRENT_ROTATION, true, 16, equipment -> LogUtils.d(
                                "BLUETOOTH MANAGER SEND CURRENT_ROTATION DISPLAY FROM RPM CHANGED --------> success"),
                        (equipment, dcError) -> LogUtils.d(
                                "BLUETOOTH MANAGER SEND CURRENT_ROTATION DISPLAY FROM RPM CHANGED --------> ERROR"));


                break;

            case EquipmentEvent.ACTION_EQUIPMENT_SEARCH:
                machineData(event.equipments);
                break;
            case EquipmentEvent.ACTION_EQUIPMENT_DISCONNECT:
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageDrawable(this.getDrawable(R.mipmap.icon_bluetooth_off));
                //  machineListAdapter.notifyDataSetChanged();
                break;

        }
    }

    ImageView imageView;
    ProgressBar progressBar;

    private void machineData(List<DCEquipment> equipments) {

        Log.e(TAG, "---------machineData----" + equipments.size());
        machineListAdapter = new MachineListAdapter(equipments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(machineListAdapter);
        machineListAdapter.setAnimationEnable(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(machineListAdapter);

        machineListAdapter.setOnItemClickListener((adapter, view, position) -> {
            imageView = view.findViewById(R.id.machine_list_connect);
            progressBar = view.findViewById(R.id.loading);
            imageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            service.connectEquipment(equipments.get(position));
            machineListAdapter.notifyDataSetChanged();
        });
    }


    @Override
    protected void onDestroy() {
        stopService(intent);
        SharedPreferenceUtils.put(this, "isConnected", "");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
