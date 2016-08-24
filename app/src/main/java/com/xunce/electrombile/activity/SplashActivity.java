package com.xunce.electrombile.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.activity.account.VerifiedActivity;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.useful.JSONUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import cn.jpush.android.api.InstrumentedActivity;

public class SplashActivity extends InstrumentedActivity {

    private final int UPDATE = 0;
    private final int UN_UPDATE = 1;
    private boolean isUpdate;
    private File file;
    private ProgressDialog progressDialog;
    private SettingManager setManager;
    Handler MyHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE:
                    Bundle bundle = msg.getData();
                    isUpdate = bundle.getBoolean("isupdate");
                    if (isUpdate) {
                        isUpdate = false;
                    } else {
                        MyHandler.sendEmptyMessageDelayed(UN_UPDATE, 2000);
                    }
                    break;
                case UN_UPDATE:

                    break;
            }
        }
    };

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
