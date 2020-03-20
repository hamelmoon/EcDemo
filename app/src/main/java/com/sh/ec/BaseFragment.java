package com.sh.ec;/**
 * Created by HouWei on 16/7/6.
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.sh.ec.event.CommonEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Author HouWei
 * Date 16/7/6
 * Time 17:36
 * Package com.yuedong.apps.treadmill.ui
 */
public abstract class BaseFragment extends Fragment {
    public int NONE_ANIMATION_ID = -1;

    protected View rootView;
    protected int mLayoutId;
    Unbinder unbind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayoutId = getRootViewLayoutId();
        rootView = inflater.inflate(mLayoutId, container, false);
        unbind = ButterKnife.bind(this, rootView);
        EventBus.getDefault().register(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        unbind.unbind();
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initSomething();
    }


    /**
     * 返回布局文件id
     *
     * @return
     */
    protected abstract int getRootViewLayoutId();

    /**
     * 一些界面的初始化操作
     */
    protected abstract void initSomething();


    /**
     * 所有的界面都接受CommonEvent
     * UI线程执行
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommonEventMain(CommonEvent event) {

    }

    /**
     * 所有的界面都接受CommonEvent
     * 后台线程执行
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onCommonEventBackground(CommonEvent event) {

    }



}
