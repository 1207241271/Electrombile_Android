package com.xunce.electrombile.activity;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


import com.xunce.electrombile.Binding;
import com.xunce.electrombile.BindingCallback;
import com.xunce.electrombile.R;

import com.xunce.electrombile.log.MyLog;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;

/**
 * Initial the camera
 * @author Ryan.Tang
 */
public class CaptureActivity extends Activity implements QRCodeView.Delegate {
    private QRCodeView qrCodeView;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private Binding binding;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg){
            qrCodeView.startSpot();
        }

    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        Intent intent = getIntent();
        final String FromActivity = intent.getStringExtra("From");
        binding = new Binding(this, FromActivity, new BindingCallback() {
            @Override
            public void startBindFail() {
                mHandler.sendEmptyMessage(1);
            }
        });

        Button btn_InputIMEI = (Button)findViewById(R.id.btn_InputIMEI);
        btn_InputIMEI.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaptureActivity.this,InputIMEIActivity.class);
                intent.putExtra("From",FromActivity);
                startActivity(intent);
            }
        });

        Button btn_Cancel = (Button)findViewById(R.id.btn_cancel_scan);
        btn_Cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CaptureActivity.this.finish();
            }
        });

        qrCodeView =(ZBarView)findViewById(R.id.zbarview);
        qrCodeView.setDelegate(this);
        qrCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        playBeepSoundAndVibrate();
        String IMEI;
        if (result.contains("IMEI")) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                IMEI = jsonObject.getString("IMEI");
            } catch (JSONException e) {
                ToastUtils.showShort(this, "扫描失败，请重新扫描！" + e.getMessage());
                MyLog.d("解析二维码", e.getMessage());
                e.printStackTrace();
                qrCodeView.startSpot();
                return;
            }
            //判断IMEI号是否是15位
            if(IMEI == null||IMEI.length()!=15){
                ToastUtils.showShort(this,"IMEI的长度不对或者为空");
                qrCodeView.startSpot();
            } else{
                ToastUtils.showShort(this, "IMEI为" + IMEI);
                binding.startBind(IMEI);
            }
        }else {
            ToastUtils.showShort(this, "请扫描小安宝二维码，请重新扫描！");
            qrCodeView.startSpot();
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }
//    private CaptureActivityHandler handler;
//    private ViewfinderView viewfinderView;
//    private boolean hasSurface;
//    private Vector<BarcodeFormat> decodeFormats;
//    private String characterSet;

////    private Button cancelScanButton;

//    };



    @Override
    protected void onResume() {
        super.onResume();
        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }
//
    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}
