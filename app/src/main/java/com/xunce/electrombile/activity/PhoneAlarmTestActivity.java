package com.xunce.electrombile.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.account.SMSandPasswordActivity;
import com.xunce.electrombile.eventbus.PhoneAlarmEvent;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.services.HttpService;
import com.xunce.electrombile.utils.useful.JSONUtils;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yangxu on 2016/11/13.
 */

public class PhoneAlarmTestActivity extends BaseActivity implements ServiceConnection{
    private ProgressDialog watiDialog;
    private HttpService.Binder httpBinder;
    private Button              btn_alarmTest;
    private Button              btn_unreceived;
    private Button              btn_alarmDelete;
    private TextView            txtView_time;
    private int                 secondleft;
    private Timer               timer;
    private TextView            textViewPhone;
    private HttpService         httpService;
    public static boolean       isStartTest;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                secondleft--;
                if (secondleft <= 0) {
                    if (timer != null) {
                        timer.cancel();
                    }
                    changeButtonState(true);
                    txtView_time.setText("60");
                } else {
                    txtView_time.setText(secondleft + "");
                }
            }else if (msg.what == 1){
                watiDialog.cancel();
                Toast.makeText(PhoneAlarmTestActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 0;
                        handler.sendMessage(message);
                    }
                }, 1000, 1000);
                changeButtonState(false);
            }else if (msg.what == 2){
                watiDialog.cancel();
                SettingManager.getInstance().setPhoneIsAgree(false);
                textViewPhone.setText("报警授权手机号为空");
                EventBus.getDefault().post(new PhoneAlarmEvent(false));
                Toast.makeText(PhoneAlarmTestActivity.this,"电话报警已关闭",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(PhoneAlarmTestActivity.this,FragmentActivity.class);
                startActivity(intent);
            }
        }

    };
    protected void onCreate(Bundle savedInstanceState)  {
        setContentView(R.layout.activity_phonealarmtest);
        super.onCreate(savedInstanceState);
        isStartTest = false;
    }

    @Override
    public void initViews() {
        View titleView = findViewById(R.id.ll_button);
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("电话报警测试");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button btn_received = (Button)findViewById(R.id.btn_received);
        btn_received.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        watiDialog = new ProgressDialog(this);
        btn_alarmTest = (Button)findViewById(R.id.btn_alarmtest);
        btn_alarmDelete = (Button)findViewById(R.id.btn_alarmdelete);
        btn_alarmTest.setOnClickListener(new myOnClickListener());
        btn_alarmDelete.setOnClickListener(new myOnClickListener());
        btn_unreceived = (Button)findViewById(R.id.btn_unreceived);
        btn_unreceived.setOnClickListener(new myOnClickListener());
        txtView_time = (TextView) findViewById(R.id.textView_timer);
        textViewPhone = (TextView) findViewById(R.id.textView_phone);
        textViewPhone.setText("报警授权手机号："+AVUser.getCurrentUser().getUsername());
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
                case R.id.btn_unreceived:
                    gotoResend();
            }
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        if (isStartTest){
            changeButtonState(true);
            isStartTest = false;
            secondleft = 60;
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
        Intent intent = new Intent(PhoneAlarmTestActivity.this, HttpService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }
    @Override
    public void onPause(){
        super.onPause();
        unbindService(this);
    }

    private void sendPostTest(){
        secondleft = 60;
        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/telephone/"+SettingManager.getInstance().getIMEI();

        if (httpService!=null){
            watiDialog.setMessage("正在设置");
            watiDialog.show();
            try {
                JSONObject caller = new JSONObject();
                int index = SettingManager.getInstance().getSavedAlarmIndex();
                caller.put("caller",index);
                httpService.dealWithHttpResponse(url,3,"phoneAlarmTest",caller.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            Toast.makeText(PhoneAlarmTestActivity.this,"连接服务开启失败",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    private void sendDelete(){
        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/telephone/" + SettingManager.getInstance().getIMEI();
        if(httpService != null){
            watiDialog.setMessage("正在设置");
            watiDialog.show();
            httpService.dealWithHttpResponse(url,2,"deletePhoneAlarm",null);
        }else {
            Toast.makeText(PhoneAlarmTestActivity.this,"连接服务开启失败",Toast.LENGTH_SHORT).show();

        }

    }

    private void gotoResend(){
        watiDialog.cancel();
        Intent intent = new Intent(PhoneAlarmTestActivity.this,ChangeAlarmActivity.class);
        startActivity(intent);
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
                if (type.equals("deletePhoneAlarm")){
                    Message message = new Message();
                    message.what = 2;
                    handler.sendMessage(message);
                }else if (type.equals("phoneAlarmTest")){
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
            @Override
            public void dealError(short errorCode) {
            }

        });
        httpService = httpBinder.getHttpService();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void changeButtonState(Boolean isTapped){
        if (isTapped){
            btn_unreceived.setEnabled(true);
            btn_unreceived.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_greenrect));
        }else {
            btn_alarmTest.setEnabled(false);
            btn_alarmTest.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_grayrect));
            btn_unreceived.setEnabled(false);
            btn_unreceived.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_grayrect));
        }
    }
}
