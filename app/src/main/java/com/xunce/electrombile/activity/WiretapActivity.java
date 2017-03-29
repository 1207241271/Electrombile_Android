package com.xunce.electrombile.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.asm.Label;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.xunce.electrombile.Manifest;
import com.xunce.electrombile.R;
import com.xunce.electrombile.eventbus.NotifiyArriviedEvent;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.services.HttpService;
import com.xunce.electrombile.utils.HttpUtil.HttpUtil;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.PermissionChecker;
import com.xunce.electrombile.view.Waveform.RendererFactory;
import com.xunce.electrombile.view.Waveform.WaveformView;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yangxu on 2017/1/12.
 */

public class WiretapActivity extends BaseActivity implements ServiceConnection{
    private Button btnPlay;
    private Button btnStop;
    private TextView titleLabel;
    private TextView cutDownLabel;
    private RecordStatus recordStatus;
    private int secondLeft;
    private Timer timer;
    private HttpService.Binder  httpBinder;
    private HttpService httpService;
    private String  APK_dir;
    MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;


    private static final String[] PERMISSIONS = new String[]{
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS
    };
    //波形
    private static final int CAPTURE_SIZE = 256; // 获取这些数据, 用于显示
    private static final int REQUEST_CODE = 0;

    private Visualizer mVisualizer;
    private WaveformView mWvWaveform;

    enum RecordStatus{
        RecordStatus_Start,
        RecordStatus_Record,
        RecordStatus_Play,
        RecordStatus_Pause,
        RecordStatus_End
    }


    Handler mHander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                secondLeft --;
                if (secondLeft <= 0){
                    if (timer != null){
                        timer.cancel();
                    }
                    changeButtonState(btnStop,false);

                }else {
                    cutDownLabel.setText(""+secondLeft);
                }
            }else if (msg.what == 8){
                secondLeft = 60;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mHander.sendEmptyMessage(0);
                    }
                }, 1000, 1000);
                changeButtonState(btnPlay,false);
                changeButtonState(btnStop,true);
                titleLabel.setText("正录音");
                btnPlay.setText("正录音");
                recordStatus = RecordStatus.RecordStatus_Record;
            }else if (msg.what == 9){
                timer.cancel();
                btnPlay.setText("暂停");
                titleLabel.setText("已结束");
                changeButtonState(btnStop,false);
                progressDialog.setMessage("正在下载");
                progressDialog.show();
            }else  if (msg.what == 11){
                try {
                    progressDialog.dismiss();
                    Bundle bundle = msg.getData();
                    String filePath = bundle.getString("filePath");
                    startVisualiser();
                    cutDownLabel.setVisibility(View.INVISIBLE);
                    changeButtonState(btnStop,true);
                    changeButtonState(btnPlay,true);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    recordStatus = RecordStatus.RecordStatus_Play;
                }catch (Exception e){

                    e.printStackTrace();
                }
            }else  if (msg.what == 110){
                progressDialog.dismiss();
                Toast.makeText(WiretapActivity.this,"下载失败", Toast.LENGTH_SHORT).show();
                resetAll();
            }else if (msg.what == 101){
                Bundle bundle = msg.getData();
                dealWithErrorCode(bundle.getInt("code"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiretap);
        initView();
    }



    @Override
    protected void onResume() {
        super.onResume();
        PermissionChecker checker = new PermissionChecker(this);
        try {
            if (checker.lakesPermissions()) {
                //todo
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent(WiretapActivity.this, HttpService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    public void initView(){

        View titleView = findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("远程窃听");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiretapActivity.this.finish();
            }
        });
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnStop = (Button) findViewById(R.id.btn_stop);
        titleLabel = (TextView) findViewById(R.id.txt_title);
        cutDownLabel = (TextView) findViewById(R.id.txt_cutdown);

        recordStatus = RecordStatus.RecordStatus_Start;

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordStatus == RecordStatus.RecordStatus_Start){
                    if (httpService != null){
                        String url = SettingManager.getInstance().getHttpHost()+ SettingManager.getInstance().getHttpPort() + "/v1/device";
                        try {
                            JSONObject cmd = new JSONObject();
                            cmd.put("c",8);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("imei",SettingManager.getInstance().getIMEI());
                            jsonObject.put("cmd",cmd);
                            httpService.dealWithHttpResponse(url,1,"recordOn",jsonObject.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }else if (recordStatus == RecordStatus.RecordStatus_Play){
                    try {
                        if (mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                            btnPlay.setText("播放");
                        }else {
                            mediaPlayer.start();
                            btnPlay.setText("暂停");
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (recordStatus  == RecordStatus.RecordStatus_Record){
                    stopWiretap();
                }else if (recordStatus == RecordStatus.RecordStatus_Play){
                    mediaPlayer.stop();
                }
            }
        });
        progressDialog = new ProgressDialog(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setText("播放");
            }
        });
        RendererFactory rendererFactory = new RendererFactory();
        mWvWaveform = (WaveformView)findViewById(R.id.wiretap_wv_waveform);
        mWvWaveform.setRenderer(rendererFactory.createSimpleWaveformRender(ContextCompat.getColor(this, R.color.red), ContextCompat.getColor(this,R.color.appgray)));

        initAPKDir();
    }

    public void stopWiretap(){
        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/device";
        try {
            JSONObject cmd = new JSONObject();
            cmd.put("c",9);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imei",SettingManager.getInstance().getIMEI());
            jsonObject.put("cmd",cmd);
            httpService.dealWithHttpResponse(url,1,"recordOff",jsonObject.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void downLoadFile(final String fileName){
        String file = fileName.split("\\.")[0];
        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/record?name=" +file;
        final String localFileName = SettingManager.getInstance().getIMEI() + "_" + (new Date()).getTime()/1000 +".amr";
        HttpHandler<File> httpHandler = new HttpUtils().download(HttpRequest.HttpMethod.GET,url, APK_dir + localFileName, null, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("filePath",APK_dir+localFileName);
                message.setData(bundle);
                message.what = 11;
                mHander.sendMessage(message);
            }
            @Override
            public void onFailure(HttpException error, String msg) {
                mHander.sendEmptyMessage(110);
                Log.d("Failure",msg);
            }
        });

    }


    private boolean isHasSdcard() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    private void initAPKDir() {
        /**
         * 创建路径的时候一定要用[/],不能使用[\],但是创建文件夹加文件的时候可以使用[\].
         * [/]符号是Linux系统路径分隔符,而[\]是windows系统路径分隔符 Android内核是Linux.
         */
        if (isHasSdcard())// 判断是否插入SD卡
        {
            APK_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Electromile/download/";// 保存到SD卡路径下
        }
        else{
            APK_dir = getApplicationContext().getFilesDir().getAbsolutePath() + "/Electromile/download/";// 保存到app的包名路径下
        }
        File destDir = new File(APK_dir);
        if (!destDir.exists()) {// 判断文件夹是否存在
            destDir.mkdirs();
        }
    }


    private void startVisualiser() {
        mVisualizer = new Visualizer(0); // 初始化
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                if (mWvWaveform != null) {
                    mWvWaveform.setWaveform(waveform);
                }
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

            }
        }, Visualizer.getMaxCaptureRate(), true, false);
        mVisualizer.setCaptureSize(CAPTURE_SIZE);
        mVisualizer.setEnabled(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onServiceConnected(final ComponentName name, IBinder service) {
        httpBinder = (HttpService.Binder) service;
        httpBinder.getHttpService().setCallback(new HttpService.Callback() {
            @Override
            public void onGetResponse(String data, String type) {
                try {
                    JSONObject object = new JSONObject(data);
                    int code = object.getInt("code");
                    if (code == 0){
                        if (type.equals("recordOn")){
                            mHander.sendEmptyMessage(8);
                        }else if (type.equals("recordOff")){
                            mHander.sendEmptyMessage(9);
                        }
                    }else {
                        Message message = new Message();
                        message.what = 101;
                        Bundle bundle = new Bundle();
                        bundle.putInt("code",code);
                        message.setData(bundle);
                        mHander.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void dealError(short errorCode) {

            }
        });
        httpService = httpBinder.getHttpService();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void changeButtonState(Button button, boolean isEnable){
        if (!isEnable){
            button.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_grayrect));
        }else if (button.equals(btnPlay)){
            button.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_greenrect));
        }else if (button.equals(btnStop)){
            button.setBackground(ContextCompat.getDrawable(this,R.drawable.btn_yellowrect));
        }
        button.setEnabled(isEnable);
    }


    @Subscribe
    public void onNotifiyArriviedEvent(NotifiyArriviedEvent event){
        String fileName =  event.getDate_str();
        if (fileName != null){
            downLoadFile(fileName);
        }
    }

    public void resetAll(){
        recordStatus = RecordStatus.RecordStatus_Start;
        changeButtonState(btnPlay,true);
        btnPlay.setText("开始录音");
        changeButtonState(btnStop,false);
        btnPlay.setText("结束");
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
        Toast.makeText(WiretapActivity.this,errStr, Toast.LENGTH_SHORT).show();
    }
}
