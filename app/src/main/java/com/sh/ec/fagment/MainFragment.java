package com.sh.ec.fagment;

import com.sh.ec.BaseFragment;
import com.sh.ec.R;
import com.sh.ec.event.DeviceEvent;
import com.sh.ec.event.EquipmentEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match

    private int mode;

    @Override
    protected int getRootViewLayoutId() {
        return R.layout.fragment_base;
    }

    @Override
    protected void initSomething() {
        EventBus.getDefault().post(new DeviceEvent(DeviceEvent.ACTION_SEARCH));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeUI(DeviceEvent event){
        switch (event.action){
            case DeviceEvent.ACTION_SEARCH:
                getChildFragmentManager().beginTransaction().replace(R.id.fragment_layout_container,new SearchFragment()).commit();
                break;
            case DeviceEvent.ACTION_RUNNING:
                if (mode==1) {
                    getChildFragmentManager().beginTransaction().replace(R.id.fragment_layout_container, new RunningFragment()).commit();
                }
                break;
            case DeviceEvent.ACTION_RESULT:
                getChildFragmentManager().beginTransaction().replace(R.id.fragment_layout_container,new ResultFragment()).commit();
                break;
            case DeviceEvent.ACTION_COUNT:
                mode=event.mode;
                getChildFragmentManager().beginTransaction().replace(R.id.fragment_layout_container,new CountDownFragment()).commit();
                break;

        }

    }


}
