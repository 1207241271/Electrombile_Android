package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.covics.zxingscanner.OnDecodeCompletionListener;
import com.covics.zxingscanner.ScannerView;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class BindingActivity2 extends Activity implements OnDecodeCompletionListener {
    private ScannerView scannerView;
    private Button btn_InputIMEI;
    private String IMEI;
    private ProgressDialog progressDialog;
    private SettingManager settingManager;
    private String FromActivity;
    private MqttConnectManager mqttConnectManager;
    public CmdCenter mCenter;
    public String previous_IMEI;
    private List<String> IMEIlist;
    public Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ToastUtils.showShort(BindingActivity2.this, "绑定设备超时");
        }
    };
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            handler_key key = handler_key.values()[msg.what];
            switch(key){
                case START_BIND:
                    progressDialog.show();
                    startBind(IMEI);
                    //超时设置
                    timeOut();
                    break;

                case SUCCESS:
                    //在cleanDevice中将setAlarmFlag设为false了,此时需要从服务器查询
                    previous_IMEI = settingManager.getIMEI();
                    settingManager.cleanDevice();
                    settingManager.setIMEI(IMEI);
                    ToastUtils.showShort(BindingActivity2.this, "设备登陆成功");
                    progressDialog.cancel();
                    getAlarmStatus();
//                    gotoAct();
                    break;

                case FAILED:
//                    times = 0;
                    progressDialog.cancel();
                    ToastUtils.showShort(BindingActivity2.this, msg.obj.toString());
                    break;

                case BIND_DEVICE_NUMBER:
                    int BindedDeviceNum = (int)msg.obj;
                    getAlarmStatus_next(BindedDeviceNum);
                    break;
            }
        }

    };


    private void getAlarmStatus_next(int BindedDeviceNum){
        if(1 == BindedDeviceNum){
            //刚刚绑定的就是第一个设备:订阅;查询
            mqttConnectManager.subscribe(settingManager.getIMEI());
            mqttConnectManager.sendMessage(mCenter.cmdFenceGet(),settingManager.getIMEI());

        }
        else if(BindedDeviceNum > 1){
            //
            if(mqttConnectManager.unSubscribe(previous_IMEI)){
                //解订阅成功
                mqttConnectManager.subscribe(settingManager.getIMEI());
                mqttConnectManager.sendMessage(mCenter.cmdFenceGet(), settingManager.getIMEI());
            }
        }
        gotoAct();
    }

    private void getAlarmStatus(){
        mCenter = CmdCenter.getInstance();
        mqttConnectManager = MqttConnectManager.getInstance();
        if(mqttConnectManager.getMac() == null){
            //说明是fragmentActivity还没有进去  可以在fragmentActivity中再去查询开关状态
            gotoAct();
        }
        else{
            //说明是fragmentActivity已经在栈中
            if(mqttConnectManager.returnMqttStatus()){
                //需要判断这个是不是绑定的第一个设备
                QueryBindList();
            }
            else{
                ToastUtils.showShort(BindingActivity2.this,"在Binding中mqtt连接断开了");
            }
        }
    }

    public void gotoAct(){
        if(FromActivity.equals("CarManageActivity")){
            getIMEIlist();
            String IMEI_first = IMEIlist.get(0);
            IMEIlist.add(IMEI_first);
            IMEIlist.set(0, settingManager.getIMEI());
            JSONArray jsonArray = new JSONArray();
            for(String IMEI:IMEIlist){
                jsonArray.put(IMEI);
            }
//            settingManager.setIMEIlist(jsonArray.toString());
            Intent intent = new Intent(BindingActivity2.this,FragmentActivity.class);
            //换位置
            startActivity(intent);
        }
        else{
            //之前还没有绑定过设备
            IMEIlist.clear();
            IMEIlist.add(settingManager.getIMEI());
            settingManager.setIMEIlist(IMEIlist);
            Intent intent = new Intent(BindingActivity2.this,WelcomeActivity.class);
            startActivity(intent);
        }
        finish();
    }

    public void getIMEIlist(){
        IMEIlist = settingManager.getIMEIlist();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding2);
        initView();
        initEvent();
    }
    private void initView() {
        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("扫描设备号二维码");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BindingActivity2.this.finish();
            }
        });


        scannerView = (ScannerView) findViewById(R.id.scanner_view);
        btn_InputIMEI = (Button)findViewById(R.id.btn_InputIMEI);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("连接中，请稍候...");
        settingManager = SettingManager.getInstance();
    }

    private void initEvent(){
        scannerView.setOnDecodeListener(this);
        btn_InputIMEI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BindingActivity2.this, InputIMEIActivity.class);
                intent.putExtra("From",FromActivity);
                startActivity(intent);
            }
        });

        //解析是从哪个activity跳转过来的
        Intent intent = getIntent();
        FromActivity = intent.getStringExtra("From");
    }

    @Override
    public void onDecodeCompletion(String barcodeFormat, String barcode, Bitmap bitmap) {
        if (barcode != null) {
            if (barcode.contains("IMEI")) {
                try {
                    IMEI = JSONUtils.ParseJSON(barcode, "IMEI");
                } catch (JSONException e) {
                    ToastUtils.showShort(BindingActivity2.this, "扫描失败，请重新扫描！");
                    e.printStackTrace();
                    return;
                }
                //判断IMEI号是否是15位
                if(IMEIlength(IMEI)){
                    mHandler.sendEmptyMessage(handler_key.START_BIND.ordinal());
                }
                else{
                    ToastUtils.showShort(BindingActivity2.this,"IMEI的长度不对");
                }
            }
        }
        else{
            //扫描失败
            ToastUtils.showShort(BindingActivity2.this, "扫描失败，请重新扫描！");
        }
    }

    //判断IMEI号是否为15位
    private Boolean IMEIlength(String IMEI){
        if(IMEI.length() == 15||IMEI.length() == 16){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.onPause();
    }

    private enum handler_key{
        START_BIND,
        SUCCESS,
        FAILED,
        BIND_MAINCAR_SUCCESS,
        BIND_DEVICE_NUMBER,
    }

    private void timeOut(){
        new Thread() {
            public void run() {
                try {
                    sleep(10000);
                    if(progressDialog.isShowing())
                        mHandler.sendEmptyMessage(handler_key.FAILED.ordinal());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void QueryBindList(){
        AVUser currentUser = AVUser.getCurrentUser();
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        query.whereEqualTo("user", currentUser);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    Message msg = Message.obtain();
                    msg.what = handler_key.BIND_DEVICE_NUMBER.ordinal();
                    msg.obj = list.size();
                    mHandler.sendMessage(msg);
                } else {
                    e.printStackTrace();
                    ToastUtils.showShort(BindingActivity2.this,"查询绑定设备数目出错");

                }
            }
        });
    }

    private void startBind(final String IMEI){
        timeHandler.sendEmptyMessageDelayed(1, 10000);
        final AVObject bindDevice = new AVObject("Bindings");
        final AVUser currentUser = AVUser.getCurrentUser();
        bindDevice.put("user", currentUser);
        AVQuery<AVObject> query = new AVQuery<>("DID");
        final AVQuery<AVObject> queryBinding = new AVQuery<>("Bindings");
        query.whereEqualTo("IMEI", this.IMEI);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(final List<AVObject> avObjects, AVException e) {
                if (e == null && avObjects.size() > 0) {
                    Log.d("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                    queryBinding.whereEqualTo("IMEI", IMEI);
                    queryBinding.whereEqualTo("user", currentUser);
                    queryBinding.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            Log.d("成功", "IMEI查询到" + list.size() + " 条符合条件的数据");
                            //一个设备可以绑定多个用户,但是这个类是唯一的，确保不重复生成相同的对象。
                            if (list.size() > 0) {
                                //因为约束条件既有user又有IMEI  所以查询结果要么是找不到,要么是找到一个
                                android.os.Message message = new android.os.Message();
                                message.what = handler_key.FAILED.ordinal();
                                message.obj = "设备已经被绑定！";
                                mHandler.sendMessage(message);
                                return;
                            }
                            bindDevice.put("device", avObjects.get(0));
                            //Log.d("tag",avObjects.get(0));
                            if (list.size() > 0) {
                                bindDevice.put("isAdmin", false);
                                ToastUtils.showShort(BindingActivity2.this, "您正在绑定附属车辆...");
                            } else {
                                bindDevice.put("isAdmin", true);
                                ToastUtils.showShort(BindingActivity2.this, "您正在绑定主车辆...");

                                //增加查询小安宝开关状态的逻辑代码

                                timeHandler.removeMessages(1);
                            }
                            bindDevice.put("IMEI", IMEI);
                            bindDevice.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        mHandler.sendEmptyMessage(handler_key.SUCCESS.ordinal());
                                    } else {
                                        Log.d("失败", "绑定错误: " + e.getMessage());
                                        android.os.Message message = new android.os.Message();
                                        message.what = handler_key.FAILED.ordinal();
                                        message.obj = e.getMessage();
                                        mHandler.sendMessage(message);
                                    }
                                }
                            });

                        }
                    });


                } else {
                    android.os.Message message = new Message();
                    message.what = handler_key.FAILED.ordinal();
                    if (e != null) {
                        Log.d("失败", "查询错误: " + e.getMessage());
                        message.obj = e.getMessage();
                    } else {
                        message.obj = "查询设备出错！";
                    }
                    mHandler.sendMessage(message);
                }
            }
        });
    }
}