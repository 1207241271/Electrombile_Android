package com.xunce.electrombile.activity;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.services.HttpService;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yangxu on 2017/1/12.
 */

public class WiretapActivity extends BaseActivity implements ServiceConnection{
    private Button btnPlay;
    private Button btnStop;
    private boolean isRecord;
    private int secondLeft;
    private Timer timer;
    private HttpService.Binder  httpBinder;
    private HttpService httpService;

    Handler mHander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                secondLeft --;
                if (secondLeft <= 0){
                    if (timer != null){
                        timer.cancel();
                    }
                    changeButtonState(btnStop,false);

                }
            }else if (msg.what == 8){
                secondLeft = 60;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mHander.sendEmptyMessage(0);
                    }
                }, 1000, 1000);
                changeButtonState(btnPlay,false);
                changeButtonState(btnStop,true);
            }else if (msg.what == 9){

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiretap);

    }

    public void initView(){

        btnPlay = (Button) findViewById(R.id.btn_play);



        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecord){
                    if (httpService != null){
                        String url = SettingManager.getInstance().getHttpHost() + SettingManager.getInstance().getHttpPort() + "/v1/device";
                        try {
                            HttpParams cmd = new BasicHttpParams();
                            cmd.setIntParameter("c", 8);
                            HttpParams param = new BasicHttpParams();
                            param.setParameter("imei",SettingManager.getInstance().getIMEI());
                            param.setParameter("cmd",cmd);
                            httpService.dealWithHttpResponse(url,1,"recordOn",param);

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });



    }

    public void stopWiretap(){
        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/device";
        try {
            HttpParams cmd = new BasicHttpParams();
            cmd.setIntParameter("c", 9);
            HttpParams param = new BasicHttpParams();
            param.setParameter("imei",SettingManager.getInstance().getIMEI());
            param.setParameter("cmd",cmd);
            httpService.dealWithHttpResponse(url,1,"recordOff",param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        httpBinder = (HttpService.Binder) service;
        httpBinder.getHttpService().setCallback(new HttpService.Callback() {
            @Override
            public void onGetResponse(String data, String type) {
                if (type.equals("recordOn")){
                    mHander.sendEmptyMessage(8);
                }else if (type.equals("recordOff")){
                    mHander.sendEmptyMessage(9);
                }
            }

            @Override
            public void dealError(short errorCode) {

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void changeButtonState(Button button, boolean isEnable){
        if (!isEnable){
            button.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_grayrect));
        }else if (button.equals(btnPlay)){
            button.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_greenrect));
        }else if (button.equals(btnStop)){
            button.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_yellowrect));
        }
        button.setEnabled(isEnable);
    }
}
