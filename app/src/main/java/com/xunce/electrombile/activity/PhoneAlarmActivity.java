package com.xunce.electrombile.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.eventbus.PhoneAlarmEvent;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.services.HttpService;
import com.xunce.electrombile.utils.system.ContractUtils;
import com.xunce.electrombile.utils.useful.PermissionChecker;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by yangxu on 2016/11/13.
 */

public class PhoneAlarmActivity extends BaseActivity implements ServiceConnection {
    private Button btn_agree;
    private ProgressDialog watiDialog;
    private HttpService.Binder httpBinder;
    private HttpService httpService;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS
    };
    public static final int PERMISSION_REQUEST_CODE = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0){
                watiDialog.cancel();
                Toast.makeText(PhoneAlarmActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                if (!SettingManager.getInstance().getHasContracter()){
                    PermissionChecker checker=new PermissionChecker(PhoneAlarmActivity.this);
                    if (!checker.lakesPermissions(PERMISSIONS)) {
                        String[] items = getResources().getStringArray(R.array.alarmPhone);
                        ContractUtils.addContract(items[SettingManager.getInstance().getSavedAlarmIndex()], getBaseContext());
                        SettingManager.getInstance().setHasContracter(true);
                    }else {
                        ActivityCompat.requestPermissions(PhoneAlarmActivity.this,PERMISSIONS,PERMISSION_REQUEST_CODE);
                    }
                }
                SettingManager.getInstance().setPhoneIsAgree(true);
                EventBus.getDefault().post(new PhoneAlarmEvent(true));
                Intent intent = new Intent(PhoneAlarmActivity.this,PhoneAlarmTestActivity.class);
                startActivity(intent);
            }else if (msg.what == 1){
                sendPost();
            }else if (msg.what == 2){
                watiDialog.cancel();
                showDialog(msg.getData().getString("telephone"));
            }
        }

    };

    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_phonealarm);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        View titleView = findViewById(R.id.ll_button);
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("电话报警设置");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        watiDialog = new ProgressDialog(this);

        btn_agree = (Button) findViewById(R.id.phoneAlarm_agree);
        btn_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGetPhoneAlarmNumber();
            }
        });

        super.initViews();
    }

    private void sendGetPhoneAlarmNumber(){
        if (httpService != null){
            String url = SettingManager.getInstance().getHttpHost() + SettingManager.getInstance().getHttpPort() + "/v1/telephone/" + SettingManager.getInstance().getIMEI();
            watiDialog.setMessage("正在设置");
            watiDialog.show();
            httpService.dealWithHttpResponse(url,0,"getPhoneAlarm",null);
        }
    }

    private void sendPost() {
        if (httpService !=null) {
            String url = SettingManager.getInstance().getHttpHost() + SettingManager.getInstance().getHttpPort() + "/v1/telephone/" + SettingManager.getInstance().getIMEI() + "?telephone=" + AVUser.getCurrentUser().getUsername();
            watiDialog.setMessage("正在设置");
            watiDialog.show();
            httpService.dealWithHttpResponse(url,1,"setPhoneAlarm",null);
        }else {
            Toast.makeText(PhoneAlarmActivity.this,"连接服务开启失败",Toast.LENGTH_SHORT).show();
        }
    }

    public void showDialog(String telephone){
        AlertDialog.Builder builder = new AlertDialog.Builder(PhoneAlarmActivity.this);
        builder.setMessage("该设备电话报警已被"+telephone+"绑定");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onResume(){
        super.onResume();
        Intent intent = new Intent(PhoneAlarmActivity.this, HttpService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public void onPause(){
        super.onPause();
        unbindService(this);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
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
                if (type.equals("setPhoneAlarm")){
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                }else if (type.equals("getPhoneAlarm")){
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        int code = 0;
                        if (jsonObject.has("code")){
                            code = jsonObject.getInt("code");
                        }
                        String phoneNum = null;
                        if (jsonObject.has("telephone")){
                            phoneNum = jsonObject.getString("telephone");
                        }
                        if (AVUser.getCurrentUser().getUsername().equals(phoneNum)||code == 101){
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }else {
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("telephone",phoneNum);
                            message.setData(bundle);
                            message.what = 2;
                            handler.sendMessage(message);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void dealError(short errorCode) {

            }
        });
        httpService = httpBinder.getHttpService();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            String[] items = getResources().getStringArray(R.array.alarmPhone);
            ContractUtils.addContract(items[SettingManager.getInstance().getSavedAlarmIndex()], getBaseContext());
            SettingManager.getInstance().setHasContracter(true);
        } else {
        }

    }

    // 判断是否拥有所有权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

}