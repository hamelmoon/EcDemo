package com.sh.ec;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sh.ec.event.EquipmentEvent;
import com.sh.ec.utils.SharedPreferenceUtils;
import com.sh.ec.utils.TimeTools;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TimerActivity extends AppCompatActivity {
    @BindView(R.id.time_txt)
    TextView time_txt;

    private Timer mTimer;
    private TimerTask mTimerTask;

    private long curTime = 0;
    private boolean isPause = true;
    private static final int WHAT = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        ButterKnife.bind(this);
    }
    @OnClick({R.id.time_start, R.id.time_pause, R.id.time_stop,R.id.time_restart})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.time_start:
                //已经结束或者还没有开始时。或者按了暂停标记。
                curTime = 0;
                isPause = true;
                if (curTime == 0 && isPause) {
                    destroyTimer();
                    initTimer();
                    mTimer.schedule(mTimerTask, 0, 1100);
                    isPause = false;
                }
                break;
            case R.id.time_pause:
                //如果 curTime == 0，则不需要执行此操

                if (!isPause) {
                    isPause = true;
                    mTimer.cancel();
                }

                break;
            case R.id.time_stop:
                if (curTime == 0) {
                    break;
                }
                curTime = 0;
                isPause = false;
                mTimer.cancel();
                break;

            case R.id.time_restart:
                if (curTime != 0 && isPause) {
                    destroyTimer();
                    initTimer();
                    mTimer.schedule(mTimerTask, 0, 1100);
                    isPause = false;
                }
                break;
        }
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
                    if (sRecLen <= 0) {
                        mTimer.cancel();
                        curTime = 0;
                        Toast.makeText(TimerActivity.this, "结束", Toast.LENGTH_SHORT).show();
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



}
