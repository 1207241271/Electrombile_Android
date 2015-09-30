package com.xunce.electrombile.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil;
import com.baidu.mapapi.model.LatLng;
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.Historys;
import com.xunce.electrombile.fragment.MaptabFragment;
import com.xunce.electrombile.fragment.SettingsFragment;
import com.xunce.electrombile.fragment.SwitchFragment;
import com.xunce.electrombile.fragment.SwitchFragment.LocationTVClickedListener;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.protocol.CmdModeSelect;
import com.xunce.electrombile.protocol.JsonKeys;
import com.xunce.electrombile.protocol.Protocol;
import com.xunce.electrombile.service.PushService;
import com.xunce.electrombile.service.UpdateAppService;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;
import com.xunce.electrombile.view.viewpager.CustomViewPager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.yunba.android.manager.YunBaManager;


/**
 * Created by heyukun on 2015/3/24.
 */

public class FragmentActivity extends android.support.v4.app.FragmentActivity implements SwitchFragment.GPSDataChangeListener, LocationTVClickedListener {
    //推送通知用的
    public static PushService pushService;
    //保存自己的实例
    public static FragmentActivity fragmentActivity;
    private static String TAG = "FragmentActivity:";
    protected CmdCenter mCenter;
    boolean isupde;
    //my service
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService实例
            pushService = ((PushService.MsgBinder) service).getService();

        }
    };
    Handler MyHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            isupde = bundle.getBoolean("isupdate");
            if (isupde) {
                upData();
                isupde = false;
            }

        }
    };
    private SwitchFragment switchFragment;
    private MaptabFragment maptabFragment;
    private SettingsFragment settingsFragment;
    //viewpager切换使用
    private CustomViewPager mViewPager;
    private RadioGroup main_radio;
    private int checkId = R.id.rbSwitch;
    //退出使用
    private boolean isExit = false;
    //接收广播
    private MyReceiver receiver;
    private SettingManager setManager;
    /**
     * The handler. to process exit()
     */
    private Handler exitHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            isExit = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        mCenter = CmdCenter.getInstance(this);
        setManager = new SettingManager(this);

        //检查版本
        checkVersion();

        //初始化界面
        initView();
        initData();
        //注册广播
        registerBroadCast();
        //判断是否需要开启服务
        startServer();
        Historys.put(this);

        fragmentActivity = this;
    }

    private void startServer() {
        if (setManager.getIMEI().isEmpty()) {
            AVQuery<AVObject> query = new AVQuery<>("Bindings");
            final AVUser currentUser = AVUser.getCurrentUser();
            query.whereEqualTo("user", currentUser);
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> avObjects, AVException e) {
                    if (e == null && avObjects.size() > 0) {
                        setManager.setIMEI((String) avObjects.get(0).get("IMEI"));
                        Log.i(TAG + "AAAAAA", setManager.getIMEI());
                        final String topic = "simcom_" + setManager.getIMEI();
                        Log.i(TAG + "SSSSSSSSSS", topic);
                        //启动服务
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent("com.xunce.electrombile.service.PushService.MSG_ACTION");
                                bindService(intent, conn, Context.BIND_AUTO_CREATE);
                                //订阅云巴推送
                                YunBaManager.subscribe(getApplicationContext(), topic, new IMqttActionListener() {

                                    @Override
                                    public void onSuccess(IMqttToken arg0) {
                                        Log.d(TAG, "Subscribe topic succeed");
                                    }

                                    @Override
                                    public void onFailure(IMqttToken arg0, Throwable arg1) {
                                        Log.d(TAG, "Subscribe topic failed");
                                    }
                                });
                            }
                        }).start();

                        Log.d("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                        ToastUtils.showShort(FragmentActivity.this, "设备查询成功");
                    } else {
                        Log.d("失败", "查询错误2: ");
                        ToastUtils.showShort(FragmentActivity.this, "请先绑定设备");
                    }
                }
            });

        } else {
            Log.i(TAG, setManager.getIMEI());
            final String topic = "simcom_" + setManager.getIMEI();
            Log.i(TAG + "SSSSSSSSSS", topic);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent("com.xunce.electrombile.service.PushService.MSG_ACTION");
                    bindService(intent, conn, Context.BIND_AUTO_CREATE);
                    YunBaManager.subscribe(getApplicationContext(), topic, new IMqttActionListener() {

                        @Override
                        public void onSuccess(IMqttToken arg0) {
                            Log.d(TAG, "Subscribe topic succeed");
                        }

                        @Override
                        public void onFailure(IMqttToken arg0, Throwable arg1) {
                            if (arg0 != null)
                                Log.i(arg0.toString(), "XXXX");
                            if (arg1 != null)
                                Log.i("AAAA", arg1.toString());
                            Log.d(TAG, "Subscribe topic failed");
                        }
                    });
                }
            }).start();

            ToastUtils.showShort(this, "登陆成功");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtils.isNetworkConnected(this)) {
            NetworkUtils.networkDialog(this, true);
        }
    }

    private void registerBroadCast() {
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xunce.electrombile.service");
        try {
            FragmentActivity.this.registerReceiver(receiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 界面初始化
     */
    private void initView() {
        main_radio = (RadioGroup) findViewById(R.id.main_radio);
        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
        switchFragment = new SwitchFragment();
        maptabFragment = new MaptabFragment();
        settingsFragment = new SettingsFragment();
    }

    private void initData() {
        List<Fragment> list = new ArrayList<>();
        list.add(switchFragment);
        list.add(maptabFragment);
        list.add(settingsFragment);
        HomePagerAdapter mAdapter = new HomePagerAdapter(getSupportFragmentManager(), list);
        mViewPager.setAdapter(mAdapter);
        main_radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rbSwitch:
                        mViewPager.setCurrentItem(0, false);
                        checkId = 0;
                        break;
                    case R.id.rbMap:
                        mViewPager.setCurrentItem(1, false);
                        checkId = 1;
                        break;
                    case R.id.rbSettings:
                        mViewPager.setCurrentItem(2, false);
                        checkId = 2;
                        break;
                    default:
                        break;
                }
            }
        });
        main_radio.check(checkId);
    }

    /**
     * 检查更新
     */
    public void upData() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("软件升级")
                .setMessage("发现新版本,建议立即更新使用.")
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent updateIntent = new Intent(FragmentActivity.this, UpdateAppService.class);
                        startService(updateIntent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alert.create().show();
    }


    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pushService != null) {
            pushService.sendMessage1(mCenter.cmdFenceGet());
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        if (!setManager.getIMEI().isEmpty()) {
            pushService.actionStop(this);
            unbindService(conn);
        }
        if (TracksManager.getTracks() != null) TracksManager.clearTracks();
        super.onDestroy();
    }


    public void checkVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                String baseUrl = "http://fir.im/api/v2/app/version/%s?token=%s";
                //下面是正式的 版本调整
                // String checkUpdateUrl = String.format(baseUrl, "553ca95096a9fc5c14001802", "39d16f30ebf111e4a2da4efe6522248a4b9d9ed4");
                String checkUpdateUrl = String.format(baseUrl, "556c810d2bb8ac0e5d001a30", "b9d54ba0b12411e4bc2c492c76a46d264a53ba2f");

                HttpClient httpClient = new DefaultHttpClient();
                //请求超时
                httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
                //读取超时
                httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
                try {
                    HttpGet httpGet = new HttpGet(checkUpdateUrl);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        String firResponse = EntityUtils.toString(httpEntity);
                        JSONObject versionJsonObj = new JSONObject(firResponse);
                        //FIR上当前的versionCode
                        int firVersionCode = Integer.parseInt(versionJsonObj.getString("version"));
                        //FIR上当前的versionName
                        String firVersionName = versionJsonObj.getString("versionShort");
                        PackageManager pm = FragmentActivity.this.getPackageManager();
                        PackageInfo pi = pm.getPackageInfo(FragmentActivity.this.getPackageName(),
                                PackageManager.GET_ACTIVITIES);
                        if (pi != null) {
                            int currentVersionCode = pi.versionCode;
                            String currentVersionName = pi.versionName;
//                            Log.i("当前版本",currentVersionCode+"");
//                            Log.i("查看版本",firVersionCode+"");
                            if (firVersionCode > currentVersionCode) {
                                //需要更新
                                bundle.putBoolean("isupdate", true);
                            } else if (firVersionCode == currentVersionCode) {
                                //如果本地app的versionCode与FIR上的app的versionCode一致，则需要判断versionName.
                                if (!currentVersionName.equals(firVersionName)) {
                                    bundle.putBoolean("isupdate", true);
                                }
                            } else {
                                //不需要更新,当前版本高于FIR上的app版本.
                                bundle.putBoolean("isupdate", false);
                            }
                            msg.setData(bundle);
                            FragmentActivity.this.MyHandler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    public void gpsCallBack(LatLng desLat, TracksManager.TrackPoint trackPoint) {
        //传递数据给地图的Fragment
        //如果正在播放轨迹，则更新位置
        //    Log.i("gpsCallBack","called");
        if (!maptabFragment.isPlaying)
            maptabFragment.locateMobile(trackPoint);
        switchFragment.reverserGeoCedec(desLat);
    }

    @Override
    public void locationTVClicked() {
        checkId = R.id.rbMap;
        main_radio.check(checkId);
        checkId = 1;
    }

    /**
     * 重复按下返回键退出app方法
     */
    public void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(),
                    "退出程序", Toast.LENGTH_SHORT).show();
            exitHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            switchFragment.cancelNotification();
            unregisterReceiver(receiver);
            //此方法会不在onDestory中调用，所以放在结束任务之前使用
            if (!setManager.getIMEI().isEmpty()) {
                pushService.actionStop(this);
                unbindService(conn);
            }
            if (TracksManager.getTracks() != null) TracksManager.clearTracks();

            //返回桌面
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
            Historys.exit();
        }
    }

    class HomePagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> list;

        public HomePagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "接收调用");
            Bundle bundle = intent.getExtras();
            String MODE = bundle.getString(CmdModeSelect.SELECT_MODE);
            Log.i(TAG, "MODE:" + MODE);
            Protocol protocol;
            protocol = (Protocol) bundle.get("protocol");
            if (CmdModeSelect.SELECT_MODE_GPS.equals(MODE)) {
                Log.i(TAG, "得到GPS");
                maptabFragment.cancelWaitTimeOut();
                onGPSArrived(protocol);
            } else if (CmdModeSelect.SELECT_MODE_CMD.equals(MODE)) {
                Log.i(TAG, "得到命令字");
                onCmdArrived(protocol);
            } else if (CmdModeSelect.SELECT_MODE_433.equals(MODE)) {
                // 找车
                on433Arrived(protocol);
            }

        }

        private void on433Arrived(Protocol protocol) {
            int intensity = protocol.getIntensity();
            caseSeekSendToFindAct(intensity);
        }

        private void onCmdArrived(Protocol protocol) {
            String cmd = protocol.getCmd();
            int result = protocol.getResult();
            switch (cmd) {
                //如果是设置围栏的命令
                case JsonKeys.FENCE_ON:
                    switchFragment.cancelWaitTimeOut();
                    caseFence(result, true, "防盗开启成功", "防盗开启失败");
                    break;
                //如果是设置关闭围栏的命令
                case JsonKeys.FENCE_OFF:
                    switchFragment.cancelWaitTimeOut();
                    caseFence(result, false, "防盗关闭成功", "防盗关闭失败");
                    break;
                //如果是获取围栏的命令
                case JsonKeys.FENCE_GET:
                    caseFenceGet(protocol, result);
                    break;
                //如果是开始找车的命令
                case JsonKeys.SEEK_ON:
                    caseSeek(result, "开始找车", "开始找车初始化失败");
                    break;
                //如果是停止找车的命令
                case JsonKeys.SEEK_OFF:
                    caseSeek(result, "停止找车", "停止找车失败");
                    break;
                default:
                    break;
            }
        }

        private void caseSeek(int result, String success, String failed) {
            if (0 == result) {
                ToastUtils.showShort(FragmentActivity.this, success);
                caseSeekSendToFindAct(0);
            } else {
                ToastUtils.showShort(FragmentActivity.this, failed);
                caseSeekSendToFindAct(0);
            }
        }

        private void caseSeekSendToFindAct(int value) {
            Intent intent7 = new Intent();
            intent7.putExtra("intensity", value);
            intent7.setAction("com.xunce.electrombile.find");
            sendBroadcast(intent7);
        }

        private void caseFenceGet(Protocol protocol, int result) {
            String state = protocol.getState();
            if (0 == result) {
                if (JsonKeys.ON.equals(state)) {
                    setManager.setAlarmFlag(true);
                    switchFragment.openStateAlarmBtn();
                } else if (JsonKeys.OFF.equals(state)) {
                    setManager.setAlarmFlag(false);
                    switchFragment.closeStateAlarmBtn();
                }
                ToastUtils.showShort(FragmentActivity.this, "查询状态成功");
            } else {
                ToastUtils.showShort(FragmentActivity.this, "查询状态失败");
            }
        }

        private void caseFence(int result, boolean successAlarmFlag, String success, String failed) {
            if (0 == result) {
                setManager.setAlarmFlag(successAlarmFlag);

                switchFragment.msgSuccessArrived();
                ToastUtils.showShort(FragmentActivity.this, success);
            } else {
                setManager.setAlarmFlag(!successAlarmFlag);
                switchFragment.msgSuccessArrived();
                ToastUtils.showShort(FragmentActivity.this, failed);
            }
        }

        private void onGPSArrived(Protocol protocol) {

            float Flat = protocol.getLat();
            float Flong = protocol.getLng();
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            TracksManager.TrackPoint trackPoint = null;
            trackPoint = new TracksManager.TrackPoint(curDate, mCenter.convertPoint(new LatLng(Flat, Flong)));
            LogUtil.log.i("保存数据1");
            setManager.setInitLocation(Flat + "", Flong + "");
            if (trackPoint != null) {
                if (!maptabFragment.isPlaying) {
                    maptabFragment.locateMobile(trackPoint);
                }
                switchFragment.reverserGeoCedec(trackPoint.point);
            }
        }
    }

}