package com.sh.ec.fagment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sh.ec.BaseFragment;
import com.sh.ec.R;
import com.sh.ec.entity.PauseCauseEnum;
import com.sh.ec.event.EquipmentEvent;
import com.sh.ec.event.SportDataChangeEvent;
import com.sh.ec.utils.TimeTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    private String TAG = "RunningFragment";

    private List<Integer>  hearts = new ArrayList<>();

    private Timer mTimer;
    private TimerTask mTimerTask;

    private static final long MAX_TIME = 12000;
    private long curTime = 0;
    private boolean isPause = false;
    private static final int WHAT = 101;
    //心率值
    int max = Integer.MIN_VALUE; //首先给定一个最小值
    int avg=0;
    int sum=0;
    //平均速度
    float avg_speed = 1;
    @Override
    protected int getRootViewLayoutId() {
        return R.layout.fragment_running;
    }

    @Override
    protected void initSomething() {
        initTimer();
        mTimer.schedule(mTimerTask, 0, 1100);
        startS();
    }

    @OnClick({R.id.start_button,R.id.pause_button,R.id.stop_button})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.start_button:
                //已经结束或者还没有开始时。或者按了暂停标记。
                if (curTime != 0 && isPause) {
                    destroyTimer();
                    initTimer();
                    mTimer.schedule(mTimerTask, 0, 1100);
                    isPause = false;
                }
                EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_RESATART));

                break;
            case R.id.pause_button:
                //如果 curTime == 0，则不需要执行此操

                if (!isPause) {
                    isPause = true;
                    mTimer.cancel();
                }
                pauseS();
                break;
            case R.id.stop_button:
                if (curTime == 0) {
                    break;
                }
                curTime = 0;
                isPause = false;
                mTimer.cancel();
                stopS();
                break;
        }
    }

    public void startS() {
        speed_txt.setText(1.0f+"");
        avg_speed_txt.setText(avg_speed+"");
        EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_QUICK_START));
    }
    public  void pauseS() {
        EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_PAUSE));
    }
    public  void stopS() {
        EventBus.getDefault().post(new EquipmentEvent(EquipmentEvent.ACTION_STOP));
    }



    /**
     * 初始化Timer
     */
    public void initTimer() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                /*if (curTime == 0) {
                    curTime = MAX_TIME;
                } else {
                    curTime -= 1000;
                }*/
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
                    if (sRecLen <= 0) {
                        mTimer.cancel();
                        curTime = 0;
                        Toast.makeText(getActivity(), "结束", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


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
    public void getSportData(SportDataChangeEvent event){
        switch (event.action){

            case SportDataChangeEvent.ACTION_CALORIE:
                calorie_txt.setText(event.bluetoothSportStats.getKcalPerHour()+"");

                break;
            case SportDataChangeEvent.ACTION_DIS:
                dis_txt.setText(event.bluetoothSportStats.getCurrentSessionCumulativeKM()+"");

                break;
            case SportDataChangeEvent.ACTION_SPEED:
                speed_txt.setText(event.bluetoothSportStats.getSpeedKmPerHour()+"");

                break;
            case SportDataChangeEvent.ACTION_AVG_SPEED:
                avg_speed = event.bluetoothSportStats.getSessionAverageSpeedChanged();
                avg_speed_txt.setText(event.bluetoothSportStats.getSessionAverageSpeedChanged()+"");
                String avgSpeedStr ="0";
                if(avg_speed>0){
                    avgSpeedStr = String.format("%.1f", 60 / avg_speed);
                    matching_speed_txt.setText(avgSpeedStr + "Min/km");
                }else{
                    matching_speed_txt.setText("0" + "Min/km");
                }

                break;

            case SportDataChangeEvent.ACTION_INCLINE:
                incline_txt.setText(event.bluetoothSportStats.getInclinePercentage()+"");

                break;
            case SportDataChangeEvent.ACTION_HEART_RATE:
                sum = 0;
                hearts.add( event.bluetoothSportStats.getAnalogHeartRate());
                //首先遍历数组中的所有数字，从第一个数开始，如果大于给定的数，则将这个数赋给max
                for(int i=0;i<hearts.size();i++){
                    if(hearts.get(i)>max){
                        max=hearts.get(i);
                    }
                //每循环一次，将结果保存在sum中
                    sum=sum+hearts.get(i);
                }
                //计算出总和后，再除以数组长度，得到平均值
                avg = sum/hearts.size();
                Log.e("Running","----avg_heart----"+avg);
                max_heart_txt.setText(max+"");
                avg_heart_txt.setText(avg+"");

                break;

        }

    }


    private Bundle bundle;

   /* public BroadcastReceiver getSportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.domyos.econnected.SEND_SPORT_DATA")) {
                // 接收到广播传来的数据
                bundle = intent.getExtras();

                if (bundle != null) {
                    refreshUI(bundle);
                } else {
                }

            } else if (action.equals("com.domyos.econnected.SEND_HEART_DATA")) {

            }
        }


    };

    private void refreshUI(Bundle bundle) {
        avg_speed_txt.setText(bundle.get("avg_speed")+"");
        Log.e("Running","--------avg_speed----"+bundle.get("avg_speed")+"");

    }*/
}
