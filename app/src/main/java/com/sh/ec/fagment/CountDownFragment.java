package com.sh.ec.fagment;

import android.util.Log;
import android.widget.ImageView;
import com.sh.ec.BaseFragment;
import com.sh.ec.R;
import com.sh.ec.event.DeviceEvent;
import com.sh.ec.event.EquipmentEvent;

import org.greenrobot.eventbus.EventBus;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;

public class CountDownFragment extends BaseFragment {
    @BindView(R.id.mCountdownImageView)
    ImageView mMCountdownImageView;


    @Override
    protected int getRootViewLayoutId() {
        return R.layout.count_down_fragment;
    }

    @Override
    protected void initSomething() {
        refreshUi();
        startTimer();

    }

    private Timer mTimer = new Timer();

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            startTimer();
        }
    }

    private void startTimer() {
        mCountDownSecond = 3;
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
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
                    mTimer.cancel();
                    if (isVisible()) {
                        EventBus.getDefault().post(new DeviceEvent(DeviceEvent.ACTION_RUNNING));
                       getFragmentManager().beginTransaction().remove(CountDownFragment.this)
                                .commitAllowingStateLoss();
                    }
                }
            }
        }, 1500, 1300);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private int mCountDownSecond = 3;

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

    public int getCountDownSecond() {
        return mCountDownSecond;
    }

    public void setCountDownSecond(int countDownSecond) {
        mCountDownSecond = countDownSecond;
        refreshUi();
    }

}
