package com.sh.ec.fagment;

import android.view.View;
import android.widget.TextView;
import com.sh.ec.BaseFragment;
import com.sh.ec.R;
import com.sh.ec.event.DeviceEvent;
import com.sh.ec.utils.SharedPreferenceUtils;

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
    @BindView(R.id.end_watt)
    TextView end_watt;

    @Override
    protected int getRootViewLayoutId() {
        return R.layout.fragment_result;
    }

    @Override
    protected void initSomething() {
        refreshUi();
    }

    private void refreshUi() {
        if(SharedPreferenceUtils.get(getActivity(),"last_avg_speed","")!=null){
            avg_speed_end_txt.setText(SharedPreferenceUtils.get(getActivity(),"last_avg_speed",""));
        }else {
            avg_speed_end_txt.setText("0.0");
        }
        if(SharedPreferenceUtils.get(getActivity(),"last__speed","")!=null){
            speed_end_txt.setText(SharedPreferenceUtils.get(getActivity(),"last__speed",""));
        }else {
            speed_end_txt.setText("0.0");
        }
        if(SharedPreferenceUtils.get(getActivity(),"last_time","")!=null){
            time_end_txt.setText(SharedPreferenceUtils.get(getActivity(),"last_time",""));
        }
        if(SharedPreferenceUtils.get(getActivity(),"last_distance","")!=null){
            String dis = SharedPreferenceUtils.get(getActivity(),"last_distance","");

            dis_end_txt.setText(dis==null?"0.0":dis);
        }else {
            dis_end_txt.setText("0.0");
        }
        if(SharedPreferenceUtils.get(getActivity(),"last_incline","")!=null){
            incline_end_txt.setText(SharedPreferenceUtils.get(getActivity(),"last_incline",""));
        }else {
            incline_end_txt.setText("0");
        }
        if(SharedPreferenceUtils.get(getActivity(),"last_calorie","")!=null){
            calorie_end_txt.setText(SharedPreferenceUtils.get(getActivity(),"last_calorie",""));
        }else {
            calorie_end_txt.setText("0");
        }
        if(SharedPreferenceUtils.get(getActivity(),"last_matching_speed","")!=null){
            matching_speed_end_txt.setText(SharedPreferenceUtils.get(getActivity(),"last_matching_speed",""));
        }else {
            matching_speed_end_txt.setText("0.0");
        }
        if(SharedPreferenceUtils.get(getActivity(),"max_heart","")!=null){
            max_heart_end_txt.setText(SharedPreferenceUtils.get(getActivity(),"max_heart",""));
        }else {
            max_heart_end_txt.setText("0");
        }
        if(SharedPreferenceUtils.get(getActivity(),"avg_heart","")!=null){
            avg_heart_end_txt.setText(SharedPreferenceUtils.get(getActivity(),"avg_heart",""));
        }else {
            avg_heart_end_txt.setText("0");
        }
        if(SharedPreferenceUtils.get(getActivity(),"last_watt","")!=null){
            end_watt.setText(SharedPreferenceUtils.get(getActivity(),"last_watt",""));
        }else {
            avg_heart_end_txt.setText("0");
        }


    }

    @OnClick({R.id.end_ok})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.end_ok:
                EventBus.getDefault().post(new DeviceEvent(DeviceEvent.ACTION_RUNNING, 1));
                getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
                break;

        }
    }



}
