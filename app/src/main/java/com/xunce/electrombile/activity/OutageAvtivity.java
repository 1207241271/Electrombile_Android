package com.xunce.electrombile.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.xunce.electrombile.R;
import com.xunce.electrombile.utils.device.VibratorUtil;
import com.xunce.electrombile.view.SlidingButton;

/**
 * Created by yangxu on 2017/1/3.
 */

public class OutageAvtivity extends BaseActivity {
    MediaPlayer mPlayer;
    //    private MqttAndroidClient mac;
    private SlidingButton mSlidingButton;
    private TextView tv_sliding;
    private TextView tv_alarm;
    private Animation operatingAnim;
    private MqttConnectManager mqttConnectManager;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            VibratorUtil.VibrateCancle(OutageAvtivity.this);
            mPlayer.stop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_alerm_outage);
        super.onCreate(savedInstanceState);
        alarm();
//        Intent intent = getIntent();
//        int type = intent.getIntExtra(ProtocolConstants.TYPE, 2);
        mqttConnectManager = MqttConnectManager.getInstance();
        //       int type = savedInstanceState.getInt("type");
    }

    private void alarm() {
        VibratorUtil.Vibrate(this, new long[]{1000, 2000, 2000, 1000}, true);
        //播放警铃
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mPlayer.setLooping(true);
        mPlayer.start();
        Message msg = Message.obtain();
        mHandler.sendMessageDelayed(msg,1000*60);
    }

    @Override
    public void initViews() {
        mSlidingButton = (SlidingButton) this.findViewById(R.id.mainview_answer_1_button);
        tv_sliding = (TextView) findViewById(R.id.tv_sliding);
        tv_alarm = (TextView) findViewById(R.id.tv_alarm);
    }
    @Override
    public void initEvents() {
//        startMqttClient();
//        mac = mqttConnectManager.getMac();
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        tv_alarm.startAnimation(operatingAnim);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mqttConnectManager.sendMessage(mCenter.cmdFenceOff(), setManager.getIMEI());
        mPlayer.stop();
        VibratorUtil.VibrateCancle(OutageAvtivity.this);
        OutageAvtivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSlidingButton.handleActivityEvent(event)) {
            //stop alarm
            tv_sliding.setText("停止告警！");
            tv_alarm.clearAnimation();
            VibratorUtil.VibrateCancle(OutageAvtivity.this);
            mPlayer.stop();
            OutageAvtivity.this.finish();
        } else {
            tv_sliding.setText("滑动关闭报警");
            tv_alarm.startAnimation(operatingAnim);
        }
        return super.onTouchEvent(event);
    }
}
