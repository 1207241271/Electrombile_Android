package com.xunce.electrombile.services;

/**
 * Created by yangxu on 2016/10/24.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.xunce.electrombile.utils.HttpUtil.HttpUtil;

public class HttpService extends Service {
    private String data = "服务器正在运行";
    private static final String Tag = "httpService";
    public  static final short  URLNULLError = 100;
    private Callback callback;
    @Override
    public void onCreate(){
        super.onCreate();
        Log.w(Tag,"In oncreate");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId){
        if (intent == null || intent.getStringExtra("url") == null){
            if (callback != null) {
                callback.dealError(URLNULLError);
            }
            return super.onStartCommand(intent,flags,startId);
        }
        final String url = intent.getStringExtra("url");
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (intent.getStringExtra("type").equals("routeInfo")) {
                    String result = HttpUtil.sendGet(url, null);
                    if (!result.isEmpty()) {
                        if (callback != null) {
                            callback.onGetRouteData(result);
                        }
                    } else {
                        //TODO: Error
                    }
                }else if (intent.getStringExtra("type").equals("gps")) {
                    String result = HttpUtil.sendGet(url, null);
                    if (!result.isEmpty()) {
                        if (callback != null) {
                            callback.onGetGPSData(result);
                        }
                    } else {
                        //TODO: Error
                    }
                }
            }
        }.start();
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent){
        Log.d(Tag,"服务器已被绑定");
        return new Binder();
    }
    public class Binder extends android.os.Binder{
        public void setData(String data){
            HttpService.this.data = data;
        }
        public HttpService getHttpService(){
            return HttpService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    public void setCallback(Callback callback){
        this.callback = callback;
    }

    public Callback getCallback() {
        return callback;
    }

    public static interface Callback{
        void onGetGPSData(String data);
        void onGetRouteData(String data);
        void dealError(short errorCode);
    }
}
