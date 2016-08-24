package com.xunce.electrombile.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.activity.account.VerifiedActivity;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.util.Random;

import cn.jpush.android.api.InstrumentedActivity;

public class SplashActivity extends InstrumentedActivity {


    private SettingManager setManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setManager = SettingManager.getInstance();
        ServiceConstants.MQTT_HOST = setManager.getServer();
        getDeviceId();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Context context = this;
        if(!NetworkUtils.isNetworkConnected(this)){
            NetworkUtils.networkDialogNoCancel(context);
        }

        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            Intent intent;
            LogUtil.log.e("verified", "verified:" + currentUser.isMobilePhoneVerified());
            if (currentUser.isMobilePhoneVerified()) {
                if(setManager.getIMEIlist().size() == 0){
                    //虽然上次退出的时候是登录状态  但是没有把设备绑定好
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    SplashActivity.this.finish();
                    return;
                }
                setManager.setFirstLogin(false);
                intent = new Intent(SplashActivity.this, FragmentActivity.class);
                startActivity(intent);
            }
            else {
                setManager.setPhoneNumber(currentUser.getUsername());
                intent = new Intent(SplashActivity.this, VerifiedActivity.class);
                startActivity(intent);
            }
            SplashActivity.this.finish();
        } else {
            setManager.setFirstLogin(true);
            //判断是否进入导航页面
            if(setManager.getNeedGuide()){
                setManager.setNeedGuide(false);
                Intent intent = new Intent(SplashActivity.this, GuideActivity.class);
                startActivity(intent);
            }
            else{
                //之后需要把这句话注释掉
//                            setManager.setNeedGuide(true);
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            SplashActivity.this.finish();
        }
    }


    /**
     * 获取设备号
     */
    private void getDeviceId() {
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        Random random = new Random(100);
        ServiceConstants.clientId = TelephonyMgr.getDeviceId();
    }


}
