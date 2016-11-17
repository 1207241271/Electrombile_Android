package com.xunce.electrombile.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.services.HttpService;

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


    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0){
                watiDialog.cancel();
                Toast.makeText(PhoneAlarmActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                if (!SettingManager.getInstance().getHasContracter()){
                    addContract();
                    SettingManager.getInstance().setHasContracter(true);
                }
                Intent intent = new Intent(PhoneAlarmActivity.this,PhoneAlarmTestActivity.class);
                startActivity(intent);
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
                sendPost();
            }
        });
        super.initViews();
    }

    private void sendPost() {
        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/telephone/" + SettingManager.getInstance().getIMEI() + "?telephone=" + AVUser.getCurrentUser().getUsername();
        watiDialog.setMessage("正在设置");
        watiDialog.show();
        Intent intent = new Intent(PhoneAlarmActivity.this, HttpService.class);
        intent.putExtra("url", url);
        intent.putExtra("httpMethod", 1);
        intent.putExtra("type", "setPhoneAlarm");
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
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
            }

            @Override
            public void onPostPhoneAlarm(String data) {
                try{
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onPostTestAlarm(String data) {

            }
        });
    }

    private void addContract(){
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = getBaseContext().getContentResolver();
        ContentValues values = new ContentValues();
        long contactId = ContentUris.parseId(resolver.insert(uri, values));

        /* 往 data 中添加数据（要根据前面获取的id号） */
        // 添加姓名
        uri = Uri.parse("content://com.android.contacts/data");
        values.put("raw_contact_id", contactId);
        values.put("mimetype", "vnd.android.cursor.item/name");
        values.put("data2", "小安宝报警");
        resolver.insert(uri, values);

        // 添加电话
        values.clear();
        values.put("raw_contact_id", contactId);
        values.put("mimetype", "vnd.android.cursor.item/phone_v2");
        values.put("data2", "2");
        values.put("data1", "01053912804");
        resolver.insert(uri, values);
    }
}