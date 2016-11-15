package com.xunce.electrombile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xunce.electrombile.R;
import com.xunce.electrombile.eventbus.BatteryTypeEvent;
import com.xunce.electrombile.manager.SettingManager;

import org.greenrobot.eventbus.Subscribe;

/**
 * Created by yangxu on 2016/11/8.
 */

public class BatteryTypeActivity extends BaseActivity{

    private RadioGroup  radioGroup;
    private Button      button;
    private int         type;
    private TextView    textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_batterytype);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        View titleView = findViewById(R.id.ll_button);
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("电池类型设置");
        textView = (TextView)findViewById(R.id.textView);
        if (SettingManager.getInstance().getBatteryType() != 0){
            textView.setText("您的电池为"+SettingManager.getInstance().getBatteryType()+"V");
        }

        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        radioGroup = (RadioGroup)this.findViewById(R.id.radioGroup_chooseType);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();
                try {
                    RadioButton radioButton = (RadioButton)findViewById(radioButtonId);
                    switch (radioButtonId){
                        case R.id.battery_0:
                            type = 0;
                                    break;
                        case R.id.battery_12:
                            type = 12;
                            break;
                        case R.id.battery_36:
                            type = 36;
                            break;
                        case R.id.battery_48:
                            type = 48;
                            break;
                        case R.id.battery_60:
                            type = 60;
                            break;
                        case R.id.battery_72:
                            type = 72;
                            break;
                        default:
                            type = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(BatteryTypeActivity.this, "正在设置电池类型", Toast.LENGTH_SHORT).show();

                MqttConnectManager.getInstance().sendMessage(mCenter.cmdBatteryTypeSet(type), SettingManager.getInstance().getIMEI(), new MqttConnectManager.Callback() {
                    @Override
                    public void onSuccess() {
//                        Toast.makeText(BatteryTypeActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(Exception e) {
                        Toast.makeText(BatteryTypeActivity.this,"电池类型设置失败",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        super.initViews();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Subscribe
    public void onBatteryTypeEvent(BatteryTypeEvent event){
        Toast.makeText(BatteryTypeActivity.this,"电池类型设置成功",Toast.LENGTH_SHORT).show();
        SettingManager.getInstance().setBatteryType(type);
    }
}
