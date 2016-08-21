package com.xunce.electrombile.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.mqtt.Connections;

/**
 * Created by heyukun on 2015/4/24.
 */
public class AboutActivity extends Activity{

    Button returnBtn;
    Button feadbackBtn;
    TextView tv_appInfo;
    String versionName;
    TextView tv_mqtt_connecitonstatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //获取版本信息
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_ACTIVITIES);
            versionName = pi.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        tv_mqtt_connecitonstatus = (TextView)findViewById(R.id.mqtt_connecitonstatus);
        returnBtn = (Button)findViewById(R.id.btn_returnFromFelp);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        feadbackBtn = (Button)findViewById(R.id.btn_feadBack);
        feadbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "安全宝客户端V" + versionName+ " - 信息反馈");
                    intent.putExtra(Intent.EXTRA_TEXT, "我的建议：");
                    intent.setData(Uri.parse("mailto:support@huakexunce.com"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "发送成功，谢谢您的反馈", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tv_appInfo = (TextView)findViewById(R.id.tv_appInfo);
        tv_appInfo.setText("安全宝 V" + versionName);

        boolean flag = Connections.getInstance(this).getConnection(ServiceConstants.handler).getConnectionOptions().isCleanSession();
        if(flag){
            tv_mqtt_connecitonstatus.setText("isCleanSession--true");
        }else{
            tv_mqtt_connecitonstatus.setText("isCleanSession--false");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
