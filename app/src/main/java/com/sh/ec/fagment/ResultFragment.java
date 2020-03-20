package com.sh.ec.fagment;

import android.view.View;
import android.widget.TextView;
import com.sh.ec.BaseFragment;
import com.sh.ec.R;
import com.sh.ec.event.DeviceEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

public class ResultFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    @BindView(R.id.end_speed)
    TextView speed_end_txt;
    @BindView(R.id.end_time)
    TextView time_end_txt;
    @BindView(R.id.end_dis)
    TextView dis_end_txt;
    @BindView(R.id.end_calorie)
    TextView calorie_end_txt;
    @BindView(R.id.end_avg_speed)
    TextView avg_speed_end_txt;
    @BindView(R.id.end_avg_heart)
    TextView avg_heart_end_txt;
    @BindView(R.id.end_incline)
    TextView incline_end_txt;
    @BindView(R.id.end_matching_speed)
    TextView matching_speed_end_txt;
    @BindView(R.id.end_max_heart)
    TextView max_heart_end_txt;

    @Override
    protected int getRootViewLayoutId() {
        return R.layout.fragment_result;
    }

    @Override
    protected void initSomething() {

    }
    @OnClick({R.id.end_ok})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.end_ok:
                EventBus.getDefault().post(new DeviceEvent(DeviceEvent.ACTION_SEARCH));
                getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
                break;

        }
    }



}
