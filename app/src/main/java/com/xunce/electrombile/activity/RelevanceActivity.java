package com.xunce.electrombile.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xunce.electrombile.Constants.HttpConstant;
import com.xunce.electrombile.R;
import com.xunce.electrombile.eventbus.http.HttpPostEvent;
import com.xunce.electrombile.manager.HttpManager;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yangxu on 2017/3/9.
 */

public class RelevanceActivity extends BaseActivity{
    private TextView txt_relevance_on;
    private ImageView img_relevance_on;
    private TextView txt_relevance_off;
    private ImageView img_relevance_off;
    private TextView txt_title;
    private boolean isLockOn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relevance);
        initView();
    }

    private void initView(){
        View titleView = findViewById(R.id.ll_button);
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("电门设防关联");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelevanceActivity.this.finish();
            }
        });

        final String url = SettingManager.getInstance().getHttpHost()+ SettingManager.getInstance().getHttpPort() + "/v1/device";
        txt_relevance_on = (TextView) findViewById(R.id.button_relevance_lock_on);
        txt_relevance_off = (TextView) findViewById(R.id.button_relevance_lock_off);

        txt_title = (TextView) findViewById(R.id.title);

        img_relevance_on = (ImageView) findViewById(R.id.img_switch_on);
        img_relevance_off = (ImageView) findViewById(R.id.img_switch_off);

        txt_relevance_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLockOn = true;
                popAlertView();
            }
        });

        img_relevance_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLockOn = true;
                popAlertView();
            }
        });

        txt_relevance_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLockOn = false;
                HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_SET_REMOTE_LOCK, getPostBody(15,0));
            }
        });

        img_relevance_off.setOnClickListener(new View.OnClickListener() {
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
            txt_title.setText("电门设关联已开启");
            img_relevance_on.setImageDrawable(getResources().getDrawable(R.drawable.img_switch_on));
            txt_relevance_on.setTextColor(getResources().getColor(R.color.gray));
            img_relevance_on.setEnabled(false);
            txt_relevance_on.setEnabled(false);

            img_relevance_off.setImageDrawable(getResources().getDrawable(R.drawable.img_switch_off));
            txt_relevance_off.setTextColor(getResources().getColor(R.color.black));
            img_relevance_off.setEnabled(true);
            img_relevance_off.setEnabled(true);
        }else {
            txt_title.setText("电门设防关联已关闭");
            img_relevance_on.setImageDrawable(getResources().getDrawable(R.drawable.img_switch_off));
            txt_relevance_on.setTextColor(getResources().getColor(R.color.black));
            img_relevance_on.setEnabled(true);
            txt_relevance_on.setEnabled(true);

            img_relevance_off.setImageDrawable(getResources().getDrawable(R.drawable.img_switch_on));
            txt_relevance_off.setTextColor(getResources().getColor(R.color.gray));
            img_relevance_off.setEnabled(false);
            img_relevance_off.setEnabled(false);
        }
    }

    private void popAlertView(){
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_relevance_tip, null);
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle_white);

        Button btn_getSignal = (Button)view.findViewById(R.id.btn_certain);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);

        //获取屏幕的宽度
        WindowManager m = this.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75);

        btn_getSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isLockOn = false;
                final String url = SettingManager.getInstance().getHttpHost()+ SettingManager.getInstance().getHttpPort() + "/v1/device";
                HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_SET_REMOTE_LOCK, getPostBody(15,1));
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.addContentView(view, new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();
        dialog.setCancelable(false);
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
        Toast.makeText(RelevanceActivity.this,errStr, Toast.LENGTH_SHORT).show();
    }
}
