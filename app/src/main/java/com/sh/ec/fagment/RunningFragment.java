package com.sh.ec.fagment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCTreadmill;
import com.sh.ec.AppContext;
import com.sh.ec.BaseFragment;
import com.sh.ec.R;
import com.sh.ec.bluetooth.manager.utils.BluetoothEquipmentConsoleUtils;
import com.sh.ec.event.DeviceEvent;
import com.sh.ec.event.EquipmentEvent;
import com.sh.ec.event.SportDataChangeEvent;
import com.sh.ec.utils.LogUtils;
import com.sh.ec.utils.SharedPreferenceUtils;
import com.sh.ec.utils.TimeTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;


public class RunningFragment extends BaseFragment {


    @BindView(R.id.speed)
    TextView speed_txt;
    @BindView(R.id.time)
    TextView time_txt;
    @BindView(R.id.dis)
    TextView dis_txt;
    @BindView(R.id.calorie)
    TextView calorie_txt;
    @BindView(R.id.avg_speed)
    TextView avg_speed_txt;
    @BindView(R.id.avg_heart)
    TextView avg_heart_txt;
    @BindView(R.id.incline)
    TextView incline_txt;
    @BindView(R.id.matching_speed)
    TextView matching_speed_txt;
    @BindView(R.id.max_heart)
    TextView max_heart_txt;
    @BindView(R.id.watt)
    TextView watt_txt;
    @BindView(R.id.resistance)
    TextView resistance_txt;
    @BindView(R.id.rpm)
    TextView rpm_txt;
    @BindView(R.id.mCountdownImageView)
    ImageView mMCountdownImageView;
    @BindView(R.id.running_layout_container)
    FrameLayout running_layout_container;

    private String TAG = "RunningFragment";

    private List<Integer> hearts = new ArrayList<>();

    private Timer mTimer;
    private TimerTask mTimerTask;

    private static final long MAX_TIME = 12000;
    private long curTime = 0;
    private boolean isPause = false;
    private static final int WHAT = 101;
    private int last_calorie;
    //时间相关
    //每公里的时间数组
    private List<Long> timeList = new ArrayList<>();

    private Long[] times;
    private long first_time;
    private long pause_time;
    private long last_time;
    //速度相关
    private List<Float> speedList = new ArrayList<>();
    private static float speeds[] = new float[0];
    private float last_speed = 1;
    private float last_avg_speed = 1;
    private float last_matching_speed = 60;

    //扬升相关
    private static float inclines[] = new float[0];
    private List<Float> inclineList = new ArrayList<>();
    private float last_incline = 0;

    //距离相关
    private static float dis[] = new float[0];
    private List<Float> disList = new ArrayList<>();
    private float last_distance;

    //心率值
    int max = Integer.MIN_VALUE; //首先给定一个最小值
    int avg = 0;
    int sum = 0;
    //平均速度
    float avg_speed = 1;
    boolean isStop = false;

    float last_resistance;
    float last_watt;
    int last_rpm;


    @Override
    protected int getRootViewLayoutId() {
        return R.layout.fragment_running;
    }

    @Override
    protected void initSomething() {

        running_layout_container.setVisibility(View.VISIBLE);
        refreshUi();
        startTimer();
        startS();

    }

    @OnClick({R.id.start_button, R.id.pause_button, R.id.stop_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_button:
                //已经结束或者还没有开始时。或者按了暂停标记。
                if (curTime != 0 && isPause) {
                    destroyTimer();
                    initTimer();
                    mTimer.schedule(mTimerTask, 0, 1100);
                    isPause = false;
                }
                running_layout_container.setVisibility(View.VISIBLE);
                refreshUi();
                startTimer();
                refreshUI();
                isStop = false;
                EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_RESTART, last_speed, last_incline));

                break;
            case R.id.pause_button:
                //如果 curTime == 0，则不需要执行此操

                if (!isPause) {
                    isPause = true;
                    mTimer.cancel();
                }
                //暂停时的时间
                pause_time = curTime;
                SharedPreferenceUtils.put(getActivity(), "pause_time", pause_time + "");
                SharedPreferenceUtils.put(getActivity(), "last_time", last_time + "");
                isStop = false;
                pauseS();
                break;
            case R.id.stop_button:
                if (curTime == 0) {
                    break;
                }
                isStop = true;
                SharedPreferenceUtils.put(getActivity(), "last_time", last_time + "");
                curTime = 0;
                isPause = false;
                mTimer.cancel();
                stopS();
                break;
        }
    }

    public void startS() {

        EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_QUICK_START));
    }

    public void pauseS() {
        EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_PAUSE));
    }

    public void stopS() {

        EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_STOP));
    }

    /**
     * 初始化Timer
     */
    public void initTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                curTime += 1100;
                Message message = new Message();
                message.what = WHAT;
                message.obj = curTime;
                mHandler.sendMessage(message);
            }
        };
        mTimer = new Timer();
    }

    /**
     * destory上次使用的
     */
    public void destroyTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT:
                    long sRecLen = (long) msg.obj;
                    //毫秒换成00:00:00格式的方式，自己写的。
                    time_txt.setText(TimeTools.getCountTimeByLong(sRecLen));

                    setClockZones(sRecLen/1000);
                    if (sRecLen <= 0) {
                        mTimer.cancel();
                        curTime = 0;
                    }
                    break;
            }
        }
    };

    private void setClockZones(long sRecLen) {

        if(AppContext.getInstance().get("connected_equipment")!=null){

            int vale1,vale2;
            if(sRecLen>3600){
                vale1 = (int)sRecLen/3600;
                vale2 = (int)(sRecLen-vale1*3600)/60;

            }else {
                vale1 = (int)sRecLen/60;
                vale2 = ((int)sRecLen-vale1*60);
            }
            LogUtils.d("========TIME=======1");
            BluetoothEquipmentConsoleUtils.setClockZones((DCEquipment) AppContext.getInstance().get("connected_equipment"),vale1,vale2);
        }








    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyTimer();
        if (mHandler != null) {
            mHandler.removeMessages(WHAT);
            mHandler = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSportData(SportDataChangeEvent event) {
        switch (event.action) {
            case SportDataChangeEvent.ACTION_RESISTANCE:
                float resistance = event.bluetoothSportStats.getResistance();
                Log.e("Running", "----ACTION_RESISTANCE----" + resistance);

                resistance_txt.setText(resistance + "");
                last_resistance =  resistance;
                SharedPreferenceUtils.put(getActivity(), "last_resistance", last_resistance + "");
                break;
            case SportDataChangeEvent.ACTION_RPM:
                int rpm = event.bluetoothSportStats.getRpm();
                Log.e("Running", "----ACTION_RPM----" + rpm);

                rpm_txt.setText(rpm + "");
                last_rpm =  rpm;
                SharedPreferenceUtils.put(getActivity(), "last_rpm", last_rpm + "");
                break;
            case SportDataChangeEvent.ACTION_CALORIE:
                float calorie = event.bluetoothSportStats.getKcalPerHour();
                calorie_txt.setText(calorie + "");
                last_calorie = (int) calorie;
                SharedPreferenceUtils.put(getActivity(), "last_calorie", last_calorie + "");
                break;
            case SportDataChangeEvent.ACTION_DIS:

                float distance = event.bluetoothSportStats.getCurrentSessionCumulativeKM();
                if ((distance - last_distance) == 1) {
                    timeList.add((curTime - last_time) / 1000);

                    times = timeList.toArray(new Long[timeList.size()]);
                    last_time = curTime;
                    last_distance = distance;
                    // SharedPreferenceUtils.put(getActivity(),"last_time",last_time+"");
                    // SharedPreferenceUtils.put(getActivity(),"last_distance",last_distance+"");

                }
                last_distance = distance;
                dis_txt.setText(distance + "");


                break;
            case SportDataChangeEvent.ACTION_SPEED:

                float s = event.bluetoothSportStats.getSpeedKmPerHour();
                last_speed = s;
                SharedPreferenceUtils.put(getActivity(), "last_speed", last_speed + "");

                ///speeds[speeds.length] = s;

                //  Log.e("RUNNING", "-------speeds-------" + speeds.length+"");

                speed_txt.setText(s + "");
                if (last_speed == 0 & isStop) {
                    //SharedPreferenceUtils.put(getActivity(),"last_time",last_time+"");
                    EventBus.getDefault().post(new DeviceEvent(DeviceEvent.ACTION_RESULT));

                }


                break;
            case SportDataChangeEvent.ACTION_AVG_SPEED:

                avg_speed = event.bluetoothSportStats.getSessionAverageSpeedChanged();

                avg_speed_txt.setText(event.bluetoothSportStats.getSessionAverageSpeedChanged() + "");
                String avgSpeedStr = "0";
                if (avg_speed > 0) {
                    avgSpeedStr = String.format("%.1f", 60 / avg_speed);
                    matching_speed_txt.setText(avgSpeedStr + "Min/km");
                } else {
                    matching_speed_txt.setText("0" + "Min/km");
                }
                last_avg_speed = avg_speed;
                last_matching_speed = 60f / avg;
                SharedPreferenceUtils.put(getActivity(), "last_avg_speed", last_avg_speed + "");
                SharedPreferenceUtils.put(getActivity(), "last_matching_speed", last_matching_speed + "");

                break;

            case SportDataChangeEvent.ACTION_INCLINE:

                float inclinePercentage = event.bluetoothSportStats.getInclinePercentage();
                last_incline = inclinePercentage;
                incline_txt.setText(inclinePercentage + "");
                SharedPreferenceUtils.put(getActivity(), "last_incline", last_incline + "");

                break;
            case SportDataChangeEvent.ACTION_HEART_RATE:
                sum = 0;
                hearts.add(event.bluetoothSportStats.getAnalogHeartRate());
                //首先遍历数组中的所有数字，从第一个数开始，如果大于给定的数，则将这个数赋给max
                for (int i = 0; i < hearts.size(); i++) {
                    if (hearts.get(i) > max) {
                        max = hearts.get(i);
                    }
                    //每循环一次，将结果保存在sum中
                    sum = sum + hearts.get(i);
                }
                //计算出总和后，再除以数组长度，得到平均值
                avg = sum / hearts.size();
                Log.e("Running", "----avg_heart----" + avg);
                max_heart_txt.setText(max + "");
                avg_heart_txt.setText(avg + "");
                SharedPreferenceUtils.put(getActivity(), "max_heart", max + "");
                SharedPreferenceUtils.put(getActivity(), "avg_heart", avg + "");
                SharedPreferenceUtils.put(getActivity(), "heart_array", hearts + "");

                break;
            case SportDataChangeEvent.ACTION_WATT:
                float w = event.bluetoothSportStats.getWatt();
                Log.e("Running", "----ACTION_WATT----" + w);

                last_watt = w;
                watt_txt.setText(last_watt + "");
                SharedPreferenceUtils.put(getActivity(), "last_watt", last_watt + "");

                break;
        }

    }


    private void refreshUI() {

        if (SharedPreferenceUtils.get(getActivity(), "last_avg_speed", "") != null) {
            avg_speed_txt.setText(last_avg_speed + "");
        }
        if (SharedPreferenceUtils.get(getActivity(), "last__speed", "") != null) {
            avg_speed_txt.setText(last_speed + "");
        }
        if (SharedPreferenceUtils.get(getActivity(), "last_time", "") != null) {
            avg_speed_txt.setText(last_time + "");
        }
        if (SharedPreferenceUtils.get(getActivity(), "last_distance", "") != null) {
            avg_speed_txt.setText(last_distance + "");
        }
        if (SharedPreferenceUtils.get(getActivity(), "last_incline", "") != null) {
            avg_speed_txt.setText(last_incline + "");
        }
        if (SharedPreferenceUtils.get(getActivity(), "last_calorie", "") != null) {
            avg_speed_txt.setText(last_calorie + "");
        }
        if (SharedPreferenceUtils.get(getActivity(), "last_matching_speed", "") != null) {
            avg_speed_txt.setText(last_matching_speed + "");
        }

    }

    private Timer timer = new Timer();

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            startTimer();
        }
    }

    private void startTimer() {
        mCountDownSecond = 3;
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mCountDownSecond--;
                if (getView() != null) {
                    getView().post(new Runnable() {
                        @Override
                        public void run() {
                            refreshUi();
                            return;
                        }
                    });
                }
                if (mCountDownSecond <= 0) {
                    timer.cancel();
                    if (isVisible()) {
                        initTimer();
                        mTimer.schedule(mTimerTask, 500, 1100);

                        //一般模式下的速度和扬升（跑步机）
                        speedList.add(1f);
                        inclineList.add(0f);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                speed_txt.setText(1.0f + "");
                                avg_speed_txt.setText(avg_speed + "");
                                running_layout_container.setVisibility(View.GONE);

                            }
                        });
                    }
                }
            }
        }, 1500, 1300);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) {
            timer.cancel();
        }
    }

    private int mCountDownSecond = 4;

    private void refreshUi() {
        if (isVisible()) {
            switch (mCountDownSecond) {
                case 1:
                    mMCountdownImageView.setImageResource(R.mipmap.daojishi_1_img);
                    break;
                case 2:
                    mMCountdownImageView.setImageResource(R.mipmap.daojishi_2_img);
                    break;
                case 3:
                    mMCountdownImageView.setImageResource(R.mipmap.daojishi_3_img);
                    break;
                default:
                    if (mMCountdownImageView != null) {
                        mMCountdownImageView.setImageResource(R.mipmap.daojishi_3_img);
                    } else {
                        Log.e("sh_11111", "mMCountdownImageView is  null");
                    }

                    break;
            }
        } else {
            Log.e("sh_11111", "StartingRunningCountdownFragment");
        }
    }


}
