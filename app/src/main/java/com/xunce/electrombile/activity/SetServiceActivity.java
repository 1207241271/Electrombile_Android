package com.xunce.electrombile.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xunce.electrombile.R;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.mqtt.Connection;

/**
 * Created by yangxu on 2016/11/15.
 */

public class SetServiceActivity extends Activity{

    private EditText    edt_Mqtt_host;
    private EditText    edt_Mqtt_port;

    private EditText    edt_Http_host;
    private EditText    edt_Http_port;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setservice);

        View titleView = findViewById(R.id.ll_button) ;
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetServiceActivity.this.finish();
            }
        });

        RadioGroup radioGroup_Mqtt = (RadioGroup) findViewById(R.id.Mqtt_Group);
        radioGroup_Mqtt.setOnCheckedChangeListener(new myOnCheckedChangeListner());
        RadioGroup radioGroup_Http = (RadioGroup) findViewById(R.id.http_Group);
        radioGroup_Http.setOnCheckedChangeListener(new myOnCheckedChangeListner());

        edt_Http_host = (EditText) findViewById(R.id.httpHost);
        edt_Http_host.setText(SettingManager.getInstance().getMQTTHost());

        edt_Http_port = (EditText) findViewById(R.id.httpPort);
        edt_Http_port.setText(SettingManager.getInstance().getMQTTPort()+"");

        edt_Mqtt_host = (EditText) findViewById(R.id.mqttHost);
        edt_Mqtt_host.setText(SettingManager.getInstance().getMQTTHost());

        edt_Mqtt_port = (EditText) findViewById(R.id.mqttPort);
        edt_Mqtt_port.setText(SettingManager.getInstance().getMQTTPort()+"");

        Button mqttButton = (Button) findViewById(R.id.btn_mqtt);
        mqttButton.setOnClickListener(new myOnClickListener());

        Button httpButton = (Button) findViewById(R.id.btn_http);
        httpButton.setOnClickListener(new myOnClickListener());
    }

    public class myOnCheckedChangeListner implements RadioGroup.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.getId() == R.id.Mqtt_Group){
                if (group.getCheckedRadioButtonId() == R.id.Mqtt_release){
                    edt_Mqtt_host.setText(SettingManager.getInstance().releaseMQTTHost);
                    edt_Mqtt_port.setText(SettingManager.getInstance().releaseMQTTPort);
                }else if (group.getCheckedRadioButtonId() == R.id.Mqtt_test){
                    edt_Mqtt_host.setText(SettingManager.getInstance().testMQTTHost);
                    edt_Mqtt_port.setText(SettingManager.getInstance().testMQTTPort);
                }
            }else if (group.getId() == R.id.http_Group){
                if (group.getCheckedRadioButtonId() == R.id.http_release){
                    edt_Http_host.setText(SettingManager.getInstance().releaseHttpHost);
                    edt_Http_port.setText(SettingManager.getInstance().releaseHttpPort);
                }else if (group.getCheckedRadioButtonId() == R.id.http_test){
                    edt_Http_host.setText(SettingManager.getInstance().testHttpHost);
                    edt_Http_port.setText(SettingManager.getInstance().testHttpPort);
                }
            }
        }
    }

    public class myOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_mqtt){
                SettingManager.getInstance().setMQTTHost(edt_Mqtt_host.getText().toString());
                SettingManager.getInstance().setMQTTPort(edt_Mqtt_port.getText().toString());
                MqttConnectManager.getInstance().initMqtt();
                MqttConnectManager.getInstance().reconnectMqtt(null);
                Toast.makeText(SetServiceActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
            }else if (v.getId() == R.id.btn_http){
                SettingManager.getInstance().setHttpHost(edt_Http_host.getText().toString());
                SettingManager.getInstance().setHttpPort(edt_Http_port.getText().toString());
                Toast.makeText(SetServiceActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
