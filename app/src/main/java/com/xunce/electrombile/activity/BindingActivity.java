package com.xunce.electrombile.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xtremeprog.xpgconnect.XPGWifiDevice;
import com.xunce.electrombile.Base.config.Configs;
import com.xunce.electrombile.R;
import com.xunce.electrombile.xpg.ui.utils.ToastUtils;

import java.util.List;

public class BindingActivity extends BaseActivity implements View.OnClickListener {
    private Button bind_btn;
    private TextView equipment_info;
    private TextView jump_bind;

    //product_key
    private EditText et_product_key;

    //passCode
    private EditText et_passCode;

    //did
    private EditText et_did;

    //bindSuccess
    private Button bindSuccess;

    //String
    private String product_key;
    private String did;
    private String passcode;
    /** The progress dialog. */
    private ProgressDialog progressDialog;

    private enum handler_key{
        START_BIND,
        SUCCESS,
        FAILED,
        LOGIN,
        GET_LIST,
    }

    //handler
    Handler mHandler = new Handler(){
      public void handleMessage(Message msg){
          super.handleMessage(msg);
          handler_key key = handler_key.values()[msg.what];
          switch (key){
              case GET_LIST:
                  mCenter.getXPGWifiSDK().getBoundDevices(setManager.getUid(),setManager.getToken(), Configs.PRODUCT_KEY);
                  break;
              case LOGIN:
                  loginDevice();
                  break;
              case START_BIND:
                  progressDialog.show();
                  startBind(passcode, did);
                  break;
              case SUCCESS:
               //   ToastUtils.showShort(BindingActivity.this, "添加成功");
                  ToastUtils.showShort(BindingActivity.this, "设备登陆成功");
                  progressDialog.cancel();
                  Intent intent = new Intent(BindingActivity.this,FragmentActivity.class);
                  startActivity(intent);
                  finish();
                  break;
              case FAILED:
                  ToastUtils.showShort(BindingActivity.this, "添加失败，请返回重试");
                  break;
          }
      }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.bindEquipment));
        setContentView(R.layout.activity_binding);
        initView();
    }

    private void initView(){
        bind_btn = (Button) findViewById(R.id.bind_btn);
        equipment_info = (TextView) findViewById(R.id.equipment_info);
        jump_bind = (TextView) findViewById(R.id.jump_bind);

        et_did = (EditText) findViewById(R.id.et_did);
        et_passCode = (EditText) findViewById(R.id.et_passCode);
        et_product_key = (EditText) findViewById(R.id.et_product_key);

        bindSuccess = (Button) findViewById(R.id.bindSuccess);
        jump_bind.setOnClickListener(this);
        bind_btn.setOnClickListener(this);
        bindSuccess.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("连接中，请稍候...");
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.bind_btn:
                Intent intent1 = new Intent(BindingActivity.this,ScannerActivity.class);
                startActivityForResult(intent1, 0x01);
                break;
            case R.id.jump_bind:
                //第一次登陆
                if(FragmentActivity.ISSTARTED == false) {
                    Intent intent2 = new Intent(BindingActivity.this, FragmentActivity.class);
                    startActivity(intent2);
                }
                this.finish();
                break;
            case R.id.bindSuccess:
//                Intent intent3 = new Intent(BindingActivity.this,FragmentActivity.class);
//                startActivity(intent3);
//                this.finish();
                break;
            default:break;
        }
    }

    private void startBind(final String passcode,final String did){
        mCenter.cBindDevice(setManager.getUid(),setManager.getToken(),did,passcode,"");
    }
    /**
     * 扫描结果处理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == 0x02 && data != null) {
            if (data.getExtras().containsKey("result")) {
               // equipment_info.setText(data.getExtras().getString("result"));
                String text = data.getExtras().getString("result");
                if (text.contains("product_key=") & text.contains("did=")
                        && text.contains("passcode=")) {
                    product_key = getParamFomeUrl(text, "product_key");
                    did = getParamFomeUrl(text,"did");
                    passcode = getParamFomeUrl(text,"passcode");
                    Log.i("",product_key+"#########"+did+"#######"+passcode);
                    et_did.setText(did);
                    et_product_key.setText(product_key);
                    et_passCode.setText(passcode);
                    bind_btn.setVisibility(View.INVISIBLE);
                    mHandler.sendEmptyMessage(handler_key.START_BIND.ordinal());
                }
            }
        }else{
            equipment_info.setText(getString(R.string.scannerFailed));
        }
    }

    private String getParamFomeUrl(String url, String param) {
        String product_key = "";
        int startindex = url.indexOf(param + "=");
        startindex += (param.length() + 1);
        String subString = url.substring(startindex);
        int endindex = subString.indexOf("&");
        if (endindex == -1) {
            product_key = subString;
        } else {
            product_key = subString.substring(0, endindex);
        }
        return product_key;
    }

    @Override
    protected void didBindDevice(int error, String errorMessage, String did) {
        Log.d("扫描结果", "error=" + error + ";errorMessage=" + errorMessage
                + ";did=" + did);
        if (error == 0) {
            mHandler.sendEmptyMessage(handler_key.GET_LIST.ordinal());
        } else {
            Message message = new Message();
            message.what = handler_key.FAILED.ordinal();
            message.obj = errorMessage;
            mHandler.sendMessage(message);
        }
    }

    /**
     * 登陆设备
     *            the xpg wifi device
     */
    private void loginDevice() {

        Log.i("绑定设备列表",devicesList.toString());
        for (int i = 0; i < devicesList.size(); i++) {
            XPGWifiDevice device = devicesList.get(i);
            if (device != null) {
                mXpgWifiDevice = device;
                mXpgWifiDevice.setListener(deviceListener);
                mXpgWifiDevice.login(setManager.getUid(), setManager.getToken());
                break;
            }

        }
    }

    @Override
    protected void didLogin(XPGWifiDevice device, int result) {
        if (result == 0) {
            mXpgWifiDevice = device;
            Log.i("进入login",device.toString());
            mHandler.sendEmptyMessage(handler_key.SUCCESS.ordinal());
        } else {
            mHandler.sendEmptyMessage(handler_key.FAILED.ordinal());
        }

    }

    @Override
    protected void didDiscovered(int error, List<XPGWifiDevice> devicesList) {
        super.didDiscovered(error, devicesList);
        this.devicesList =  devicesList ;
        Log.i("设备列表",devicesList.toString());
        mHandler.sendEmptyMessage(handler_key.LOGIN.ordinal());
    }
}
