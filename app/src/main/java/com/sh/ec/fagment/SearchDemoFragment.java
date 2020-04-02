package com.sh.ec.fagment;

import android.app.ProgressDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCEquipmentManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.sh.ec.AppContext;
import com.sh.ec.BaseFragment;
import com.sh.ec.R;
import com.sh.ec.adapter.MachineListAdapter;
import com.sh.ec.adapter.MachineListDemoAdapter;
import com.sh.ec.bluetooth.BluetoothConnectionState;
import com.sh.ec.entity.ClickEntity;
import com.sh.ec.event.DeviceEvent;
import com.sh.ec.event.EquipmentEvent;
import com.sh.ec.service.MangerService;
import com.sh.ec.service.SportDataService;
import com.sh.ec.utils.SharedPreferenceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;

public class SearchDemoFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match

    @BindView(R.id.device_recyclerView)
    RecyclerView recyclerView;


    private int mColumnCount = 1;
    private String TAG = "SearchDemoFragment";
    ProgressDialog mProgressDialog = null;
    private MachineListAdapter machineListAdapter;
    private DCEquipment equipment;
    private int connectedPosition = -1;

    MangerService service;
    private boolean isConnected;

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
        service = new MangerService();
        service.startScan();
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
                isConnected =true;
                connectedPosition = list.indexOf(event.ewEquipment);
                machineListAdapter.setConnectedPosition(connectedPosition);
                machineListAdapter.notifyDataSetChanged();
                SharedPreferenceUtils.put(getActivity(), "isConnected", event.ewEquipment.getName());
                AppContext.getInstance().put("connected_equipment",event.ewEquipment);
                removeFragment();
                break;

            case EquipmentEvent.ACTION_EQUIPMENT_SEARCH:
                machineData(event.equipments);
                break;
            case EquipmentEvent.ACTION_EQUIPMENT_DISCONNECT:
                Log.e(TAG,"-----disconnectEquipment-------");
                machineListAdapter.setConnectedPosition(-1);
                machineListAdapter.notifyDataSetChanged();
                if (connectedPosition!=mPosition){
                    service.connectEquipment(list.get(mPosition).getName());
                }
                break;

            case EquipmentEvent.ACTION_EQUIPMENT_FAIL:
                Log.e(TAG,"设备连接失败");
                if(event.msg.equals("equipmentLost")){
                    if(isConnected){
                        machineListAdapter.setConnectedPosition(-1);
                        machineListAdapter.notifyDataSetChanged();
                        list.remove(connectedPosition);
                        //service.startScan();
                    }

                }
               // machineData(event.equipments);
                break;
        }
    }


    private List<DCEquipment> list = new ArrayList<>();
    private int mPosition;
    private void machineData(List<DCEquipment> equipments) {

        Log.e(TAG, "---------machineData----"+ equipments.size());
        list = equipments;
        machineListAdapter = new MachineListAdapter(equipments);
        //machineListAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(machineListAdapter);
        machineListAdapter.setAnimationEnable(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(machineListAdapter);

        machineListAdapter.setOnItemClickListener((adapter, view, position) -> {
            mPosition = position;
            if(isConnected){
                service.disconnectEquipment();
                isConnected = false;
            }else {
                Log.e(TAG,"-----connectEquipment-------");
                service.connectEquipment(equipments.get(position).getName());
            }
            machineListAdapter.setSelectedPosition(position);

           /* if (SharedPreferenceUtils.get(getActivity(), "isConnected", "") != null && !SharedPreferenceUtils.get(getActivity(), "isConnected", "").equals("")) {
                if (SharedPreferenceUtils.get(getActivity(), "isConnected", "").equals( equipments.get(position).getName())) {
                    SharedPreferenceUtils.put(getActivity(), "isConnected", "");
                    service.disconnectEquipment();
                } else {
                    service.disconnectEquipment();
                    service.connectEquipment(equipments.get(position).getName());
                }

            } else {
                service.connectEquipment(equipments.get(position).getName());
            }*/

            machineListAdapter.notifyDataSetChanged();

        });
    }


    public void removeFragment() {

       EventBus.getDefault().post(new DeviceEvent(DeviceEvent.ACTION_RUNNING, 1));
        getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (DCEquipmentManager.getInstance().isScanning())
            DCEquipmentManager.getInstance().stopScanEquipments();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }


}
