package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.baidu.mapapi.model.LatLng;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.eventbus.EmptyEvent;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * Created by lybvinci on 2015/4/24.
 */
public class BaseFragment extends Fragment{

    private static String TAG = "BaseFragmet";
    //判断是否关闭页面
    public boolean close = false;
    protected CmdCenter mCenter;
    protected SettingManager setManager;
    protected GPSDataChangeListener mGpsChangedListener;
    protected FragmentActivity m_context;

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setManager = SettingManager.getInstance();
        mCenter = CmdCenter.getInstance();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

   @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            EventBus.getDefault().register(this);
        } else {
            EventBus.getDefault().unregister(this);
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mGpsChangedListener = (GPSDataChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement GPSDataChangeListener");
        }
        m_context = (FragmentActivity) activity;
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close = true;
    }

    @Override
    public void onStop() {
//        EventBus.getDefault().unregister(this);
        close = true;
        super.onStop();
    }
    @Override
    public void onStart(){
        super.onStart();
//        EventBus.getDefault().register(this);
    }


    public interface GPSDataChangeListener {
        void gpsCallBack(LatLng desLat, TracksManager.TrackPoint trackPoint);
    }
    @Subscribe
    public void onEmptyEvent(EmptyEvent event){

    }
}

