package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.eventbus.PhoneAlarmEvent;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.services.HttpService;
import com.xunce.electrombile.utils.system.ContractUtils;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yangxu on 2016/12/19.
 */

public class ChangeAlarmActivity extends BaseActivity implements ServiceConnection{
    private HttpService.Binder httpBinder;
    private HttpService         httpService;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

            if (msg.what == 0){
                try {
                    PhoneAlarmTestActivity.isStartTest = true;
                    finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_resend_test);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        Intent intent = new Intent(ChangeAlarmActivity.this, HttpService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }
    @Override
    public void onPause(){
        super.onPause();
        unbindService(this);
    }

    @Override
    public void initViews() {
        super.initViews();
        Button button = (Button)findViewById(R.id.btn_resend);
        try {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContractUtils.deleteContract(getBaseContext());
                    String[] items = getResources().getStringArray(R.array.alarmPhone);
                    int index = SettingManager.getInstance().getSavedAlarmIndex();
                    index = index + 1;
                    if (index >= items.length){
                        index = 0;
                    }
                    SettingManager.getInstance().setSavedAlarmIndex(index);
                    ContractUtils.addContract(items[index],getBaseContext());
                    String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/test/"+ AVUser.getCurrentUser().getUsername();

                    if (httpService!=null){
                        HttpParams httpParams = new BasicHttpParams().setParameter("caller",SettingManager.getInstance().getSavedAlarmIndex());
                        httpService.dealWithHttpResponse(url,1,"phoneAlarmTest",httpParams);

                    }else {
                        Toast.makeText(ChangeAlarmActivity.this,"连接服务开启失败",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        httpBinder = (HttpService.Binder) iBinder;
        httpBinder.getHttpService().setCallback(new HttpService.Callback(){
            @Override
            public void onGetResponse(String data,String type){
                if (type.equals("phoneAlarmTest")){
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                }
            }
            @Override
            public void dealError(short errorCode) {
            }

        });
        httpService = httpBinder.getHttpService();
    }

}
