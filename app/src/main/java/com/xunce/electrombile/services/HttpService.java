package com.xunce.electrombile.services;

/**
 * Created by yangxu on 2016/10/24.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.avos.avoscloud.okhttp.internal.framed.ErrorCode;
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
        void onPostPhoneAlarm(String data);
        void onDeletePhoneAlarm(String data);
        void onPostTestAlarm(String data);
        void dealError(short errorCode);
    }

    public void dealWithHttpResponse(String url,int method,String type,String contentbody){
        final String urlFinal = url;
        final int    httpMethod =  method;
        final String typeFinal = type;
        final String body = contentbody;
        new Thread() {
            @Override
            public void run() {
                super.run();
                switch (httpMethod){
                    case 0:
                        dealWithHttpGet(urlFinal,typeFinal);
                        break;
                    case 1:
                        dealWithHttpPost(urlFinal,typeFinal,body);
                        break;
                    case 2:
                        dealWithHttpDelete(urlFinal,typeFinal);
                        break;
                }
            }
        }.start();
    }

    private void dealWithHttpGet(String url,String type){
        String result = HttpUtil.sendGet(url,null);
        if (result == null || result.isEmpty()){
            callback.dealError(URLNULLError);
        }
        if (type.equals("routeInfo")){
            callback.onGetRouteData(result);
        }else if (type.equals("gps")){
            callback.onGetGPSData(result);
        }
    }

    private void dealWithHttpPost(String url,String type,String body){
        String result = HttpUtil.sendPost(url,body);
        if (result == null || result.equals("error")){
            callback.dealError(URLNULLError);
        }
        if (type.equals("setPhoneAlarm")){
            callback.onPostPhoneAlarm(result);
        }else if (type.equals("phoneAlarmTest")){
            callback.onPostTestAlarm(result);
        }
    }

    private void dealWithHttpDelete(String url,String type){
        String result = HttpUtil.sendDelete(url,null);
        if (result == null || result.equals("error")){
            callback.dealError(URLNULLError);
        }
        if (type.equals("deletePhoneAlarm")){
            callback.onDeletePhoneAlarm(result);
        }
    }


}
