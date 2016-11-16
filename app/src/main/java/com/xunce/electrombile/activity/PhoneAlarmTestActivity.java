package com.xunce.electrombile.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.account.SMSandPasswordActivity;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.services.HttpService;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yangxu on 2016/11/13.
 */

public class PhoneAlarmTestActivity extends BaseActivity implements ServiceConnection{
    private ProgressDialog watiDialog;
    private HttpService.Binder httpBinder;
    private AlertDialog         dialog;
    private Button              btn_alarmTest;
    private Button              btn_unreceived;
    private Button              btn_alarmDelete;
    private TextView            txtView_time;
    private int                 secondleft;
    private Timer               timer;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            secondleft--;
            if (secondleft <= 0) {
                timer.cancel();
//                        btn_ResendSysCode.setEnabled(true);
//                        btn_ResendSysCode.setTextColor(Color.parseColor("#1dcf94"));
                changeButtonState(true);
                txtView_time.setText("60秒");
            } else {
                txtView_time.setText(secondleft + "秒");
            }
        }

    };
    protected void onCreate(Bundle savedInstanceState)  {
        setContentView(R.layout.activity_phonealarmtest);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        dialog = new AlertDialog.Builder(this)
                .setPositiveButton("稍后再查",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                watiDialog.dismiss();
                            }
                        }).create();
        watiDialog = new ProgressDialog(this);
        btn_alarmTest = (Button)findViewById(R.id.btn_alarmtest);
        btn_unreceived = (Button)findViewById(R.id.btn_havereveived);
        btn_alarmDelete = (Button)findViewById(R.id.btn_alarmdelete);
        txtView_time = (TextView) findViewById(R.id.textView_timer);
        super.initViews();
    }



    public class myOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            watiDialog.setMessage("正在设置");
            watiDialog.show();
            int buttonId = v.getId();
            switch (buttonId){
                case R.id.btn_alarmtest:
                    sendPostTest();
                    break;
                case R.id.btn_alarmdelete:
                    sendDelete();
                    break;
            }

        }
    }

    private void sendPostTest(){
        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/test/"+AVUser.getCurrentUser().getUsername();
        watiDialog.setMessage("正在设置");
        watiDialog.show();
        Intent intent = new Intent(PhoneAlarmTestActivity.this, HttpService.class);
        intent.putExtra("url", url);
        intent.putExtra("httpMethod", 1);
        intent.putExtra("type", "phoneAlarmTest");
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    private void sendDelete(){
        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance()+"/v1/telephone/" + SettingManager.getInstance().getIMEI() + "?telephone=" + AVUser.getCurrentUser().getUsername();
        watiDialog.setMessage("正在设置");
        watiDialog.show();
        Intent intent = new Intent(PhoneAlarmTestActivity.this, HttpService.class);
        intent.putExtra("url", url);
        intent.putExtra("httpMethod", 2);
        intent.putExtra("type", "setPhoneAlarm");
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        httpBinder = (HttpService.Binder) iBinder;
        httpBinder.getHttpService().setCallback(new HttpService.Callback(){
            @Override
            public void onGetGPSData(String data){
            }

            @Override
            public void onGetRouteData(String data){
            }

            @Override
            public void dealError(short errorCode) {
            }

            @Override
            public void onDeletePhoneAlarm(String data) {
                try{
                    watiDialog.cancel();
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.has("code")){
                        int code = jsonObject.getInt("code");
                        if (code == 102 || code == 101){
                            dialog.setTitle("解除电话报警成功");
                            dialog.show();
                        }else if (code == 0){
                            dialog.setTitle("解除电话报警成功");
                            dialog.show();
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onPostPhoneAlarm(String data) {
            }

            @Override
            public void onPostTestAlarm(String data) {
                try{
                    watiDialog.cancel();
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.has("code")){
                        int code = jsonObject.getInt("code");
                        if (code == 102 || code == 101){
                            dialog.setTitle("报警测试失败");
                            dialog.show();
                        }else if (code == 0){
                            dialog.setTitle("报警发送成功");
                            dialog.show();
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    handler.sendEmptyMessage(0);
                                }
                            }, 1000, 1000);
                            changeButtonState(true);
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    private void changeButtonState(Boolean isTapped){
        if (isTapped){
            btn_unreceived.isEnabled();
            btn_unreceived.setBackgroundColor(getResources().getColor(R.color.green));
            btn_alarmTest.isEnabled();
            btn_alarmTest.setBackgroundColor(getResources().getColor(R.color.green));
        }else {
            btn_unreceived.isEnabled();
            btn_unreceived.setBackgroundColor(getResources().getColor(R.color.gray));
            btn_alarmTest.isEnabled();
            btn_alarmTest.setBackgroundColor(getResources().getColor(R.color.gray));
        }
    }
}
