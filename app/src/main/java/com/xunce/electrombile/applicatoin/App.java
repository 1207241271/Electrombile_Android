package com.xunce.electrombile.applicatoin;

import android.app.Application;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.baidu.mapapi.SDKInitializer;
import com.xunce.electrombile.database.DBManage;
import com.xunce.electrombile.log.MyLog;

import cn.jpush.android.api.JPushInterface;


/**
 * Created by jk on 2015/3/23.
 */
public class App extends Application {
    private static final String TAG = "App";

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        //initial the Baidu map SDK
        initBaiduSDK();
        //初始化leacloud
        AVOSCloud.initialize(this,
                "5wk8ccseci7lnss55xfxdgj9xn77hxg3rppsu16o83fydjjn",
                "yovqy5zy16og43zwew8i6qmtkp2y6r9b18zerha0fqi5dqsw");

        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);

        AVAnalytics.enableCrashReport(this, true);
        AVAnalytics.setAnalyticsEnabled(true);

        MyLog.delFile();
        DBManage.updateDatabase();
    }

    private void initBaiduSDK() {
        SDKInitializer.initialize(this);
    }
}
