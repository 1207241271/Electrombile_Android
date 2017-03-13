package com.xunce.electrombile.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class RelevanceActivity extends BaseActivity{
    private Button btn_relevance;
    private boolean isRelevance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relevance);
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
                RelevanceActivity.this.finish();
            }
        });
        btn_relevance = (Button) findViewById(R.id.button_relevance);
        btn_relevance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = SettingManager.getInstance().getHttpHost()+ SettingManager.getInstance().getHttpPort() + "/v1/device";
                if (isRelevance) {
                    HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_SET_RELEVANCE, getPostBody(22,0));
                }else {
                    HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_SET_RELEVANCE, getPostBody(22,1));

                }
            }
        });

        String url = SettingManager.getInstance().getHttpHost()+ SettingManager.getInstance().getHttpPort() + "/v1/device";
        HttpManager.postHttpResult(url, HttpManager.postType.POST_TYPE_DEVICE, HttpConstant.HttpCmd.HTTP_CMD_GET_RELEVANCE, getPostBody(23,2));

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
                        if (isRelevance){
                            isRelevance = !isRelevance;
                            btn_relevance.setText("已解除");
                        }else {
                            isRelevance = !isRelevance;
                            btn_relevance.setText("已锁车");
                        }
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
                            btn_relevance.setText("已锁死");
                            isRelevance = true;
                        }else {
                            btn_relevance.setText("已解除");
                            isRelevance = false;
                        }
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
