package com.sh.ec.fagment;

import android.app.ProgressDialog;
import android.util.Log;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentManager;
import com.appdevice.domyos.DCTreadmill;
import com.sh.ec.BaseFragment;
import com.sh.ec.R;
import com.sh.ec.adapter.MachineListAdapter;
import com.sh.ec.entity.DeviceData;
import com.sh.ec.event.DeviceEvent;
import com.sh.ec.event.EquipmentEvent;
import com.sh.ec.service.SportDataService;
import com.sh.ec.service.SportDataServiceDemo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;

public class SearchFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match

    @BindView(R.id.device_recyclerView)
    RecyclerView recyclerView;


    private int mColumnCount = 1;
    private String TAG = "SearchFragment";
    ProgressDialog mProgressDialog = null;
    private MachineListAdapter machineListAdapter;
    private DCEquipment equipment;

    SportDataService service;
    //SportDataServiceDemo service;
    @Override
    protected int getRootViewLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected void initSomething() {

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("searching...");
        mProgressDialog.show();
        service = new SportDataService();
        //service = new SportDataServiceDemo();


        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                service.scan(getActivity());
            }
        };
        timer.schedule(timerTask,1000);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    /**
     * 设备连接情况
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void equipmentEvent(EquipmentEvent event) {
        switch (event.action) {
            case EquipmentEvent.ACTION_EQUIPMENT_CONNECTED:
                removeFragment();
                break;

            case EquipmentEvent.ACTION_EQUIPMENT_SEARCH:
                machineData(event.equipments);
                break;

        }
    }

    private void machineData(List<DCEquipment> equipments) {
        machineListAdapter = new MachineListAdapter(equipments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(machineListAdapter);
        machineListAdapter.setOnItemClickListener((adapter, view, position) -> {
            equipment = equipments.get(position);
            machineListAdapter.setSelectedPosition(position);
            machineListAdapter.notifyDataSetChanged();
            service.connectEquipment(equipment);


        });
    }

    public void removeFragment() {
        if(mProgressDialog!=null|mProgressDialog.isShowing()){
            mProgressDialog.cancel();
            mProgressDialog = null;
        }
        EventBus.getDefault().post(new DeviceEvent(DeviceEvent.ACTION_RUNNING, 1));
        getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        if (mProgressDialog!=null&&mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (DCEquipmentManager.getInstance().isScanning())
            DCEquipmentManager.getInstance().stopScanEquipments();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mProgressDialog!=null&&mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }


}
