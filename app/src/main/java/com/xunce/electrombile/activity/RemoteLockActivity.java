package com.xunce.electrombile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xunce.electrombile.Constants.HttpConstant;
import com.xunce.electrombile.R;
import com.xunce.electrombile.eventbus.http.HttpPostEvent;
import com.xunce.electrombile.manager.HttpManager;
import com.xunce.electrombile.manager.SettingManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangxu on 2017/3/9.
 */

public class RemoteLockActivity extends BaseActivity {
    private TextView txt_remote_on;
    private ImageView img_remote_on;
    private TextView txt_remote_off;
    private ImageView img_remote_off;
    private TextView txt_title;
    private boolean isLockOn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_lock);
        initView();
    }

    private void initView(){
        View titleView = findViewById(R.id.ll_button);
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("远程锁车");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoteLockActivity.this.finish();
            }
        });

        final String url = SettingManager.getInstance().getHttpHost()+ SettingManager.getInstance().getHttpPort() + "/v1/device";
        txt_remote_on = (TextView) findViewById(R.id.button_remote_lock_on);
        txt_remote_off = (TextView) findViewById(R.id.button_remote_lock_off);

        txt_title = (TextView) findViewById(R.id.title);

        img_remote_on = (ImageView) findViewById(R.id.img_switch_on);
        img_remote_off = (ImageView) findViewById(R.id.img_switch_off);

        txt_remote_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLockOn = true;
                HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_SET_REMOTE_LOCK, getPostBody(15,1));
            }
        });

        img_remote_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLockOn = true;
                HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_SET_REMOTE_LOCK, getPostBody(15,1));
            }
        });

        txt_remote_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLockOn = false;
                HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_SET_REMOTE_LOCK, getPostBody(15,0));
            }
        });

        img_remote_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLockOn = false;
                HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_SET_REMOTE_LOCK, getPostBody(15,0));
            }
        });

        HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_GET_REMOTE_LOCK, getPostBody(24,2));

    }


    public String getPostBody(int code,int sw){
        try {
            if (sw == 1 || sw == 0) {
                JSONObject param = new JSONObject();
                param.put("sw", sw);
                JSONObject cmd = new JSONObject();
                cmd.put("c", code);
                cmd.put("param", param);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("imei", SettingManager.getInstance().getIMEI());
                jsonObject.put("cmd", cmd);
                return jsonObject.toString();
            }else {
                JSONObject cmd = new JSONObject();
                cmd.put("c", code);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("imei", SettingManager.getInstance().getIMEI());
                jsonObject.put("cmd", cmd);
                return jsonObject.toString();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return "";
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHttpPostEvent(HttpPostEvent event){
        if (event.getCmdType() == HttpConstant.HttpCmd.HTTP_CMD_SET_REMOTE_LOCK){
            try {
                JSONObject jsonObject = new JSONObject(event.getResult());
                if (jsonObject.has("code")) {
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        changeStatus(isLockOn);
                    }else {
                        dealWithErrorCode(code);
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        }else if (event.getCmdType() == HttpConstant.HttpCmd.HTTP_CMD_GET_REMOTE_LOCK){
            try {
                JSONObject jsonObject = new JSONObject(event.getResult());
                if (jsonObject.has("code")) {
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        JSONObject result = jsonObject.getJSONObject("result");
                        int sw = result.getInt("sw");
                        if (sw == 1){
                            isLockOn = true;
                        }else {
                            isLockOn = false;
                        }
                        changeStatus(isLockOn);
                    }else {
                        dealWithErrorCode(code);
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void changeStatus(boolean isLockOn){
        Toast.makeText(this,"设置完成",Toast.LENGTH_SHORT).show();
        if (isLockOn){
            txt_title.setText("远程锁车已开启");
            img_remote_on.setImageDrawable(getResources().getDrawable(R.drawable.img_switch_on));
            txt_remote_on.setTextColor(getResources().getColor(R.color.gray));
            img_remote_on.setEnabled(false);
            txt_remote_on.setEnabled(false);

            img_remote_off.setImageDrawable(getResources().getDrawable(R.drawable.img_switch_off));
            txt_remote_off.setTextColor(getResources().getColor(R.color.black));
            img_remote_off.setEnabled(true);
            img_remote_off.setEnabled(true);
        }else {
            txt_title.setText("远程锁车已关闭");
            img_remote_on.setImageDrawable(getResources().getDrawable(R.drawable.img_switch_off));
            txt_remote_on.setTextColor(getResources().getColor(R.color.black));
            img_remote_on.setEnabled(true);
            txt_remote_on.setEnabled(true);

            img_remote_off.setImageDrawable(getResources().getDrawable(R.drawable.img_switch_on));
            txt_remote_off.setTextColor(getResources().getColor(R.color.gray));
            img_remote_off.setEnabled(false);
            img_remote_off.setEnabled(false);
        }
    }

    public void dealWithErrorCode(int code){
        String errStr = "";
        switch (code) {
            case 100:
                errStr = "服务器内部错误";
                break;
            case 101:
                errStr = "请求设备号错误";
                break;
            case 102:
                errStr = "无请求内容";
                break;
            case 103:
                errStr = "请求内容错误";
                break;
            case 104:
                errStr = "请求 URL 错误";
                break;
            case 105:
                errStr = "请求范围过大";
                break;
            case 106:
                errStr = "服务器无响应";
                break;
            case 107:
                errStr = "服务器不在线";
                break;
            case 108:
                errStr = "设备无响应";
                break;
            case 109:
                errStr = "设备不在线";
                break;
            case 110:
                errStr = "设备不在线";
                break;
            case 111:
                errStr = "您的设备不支持该操作";
                break;
            default:
                errStr = "操作不成功";
                break;
        }
        Toast.makeText(RemoteLockActivity.this,errStr, Toast.LENGTH_SHORT).show();
    }

}
