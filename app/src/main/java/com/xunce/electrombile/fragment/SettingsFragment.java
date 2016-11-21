package com.xunce.electrombile.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.AboutActivity;
import com.xunce.electrombile.activity.Autolock;
import com.xunce.electrombile.activity.BatteryTypeActivity;
import com.xunce.electrombile.activity.CarManageActivity;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.activity.HelpActivity;
import com.xunce.electrombile.activity.MapOfflineActivity;
import com.xunce.electrombile.activity.MqttConnectManager;
import com.xunce.electrombile.activity.PhoneAlarmActivity;
import com.xunce.electrombile.activity.PhoneAlarmTestActivity;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.activity.account.PersonalCenterActivity;
import com.xunce.electrombile.eventbus.BatteryTypeEvent;
import com.xunce.electrombile.eventbus.PhoneAlarmEvent;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class SettingsFragment extends BaseFragment implements View.OnClickListener {
    private static String TAG = "SettingsFragment";
    //临时变量
    public int temp = 0;
    //缓存view
    private View rootView;
    private TextView tv_autolockstatus;
    private TextView tv_batteryType;
    private TextView tv_phoneAlarm;
    private MqttConnectManager mqttConnectManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.settings_fragment, container, false);
            initView(rootView);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAutolockStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach(){
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            //帮助条目
//            case R.id.layout_help:
//                Intent intentHelp = new Intent(m_context, HelpActivity.class);
//                startActivity(intentHelp);
//                break;
            //退出登录条目
            case R.id.btn_logout:
                if (!NetworkUtils.isNetworkConnected(m_context)) {
                    ToastUtils.showShort(m_context, "请先连接网络!");
                    return;
                }
                if (!hasUser()) {
                    return;
                }

                setManager.setFirstLogin(true);
                loginOut();
                break;
            //关于
            case R.id.layout_about:
                Intent intentAbout = new Intent(m_context, AboutActivity.class);
                startActivity(intentAbout);
                break;

            //车主信息
            case R.id.layout_person_center:
                if (!hasUser()) {
                    return;
                }
                goToPersonCenterAct();
                break;

            //设备管理
            case R.id.rl_1:
                if (!hasUser()) {
                    return;
                }
                goToDeviceAct();
                break;

            //自动落锁
            case R.id.layout_autolock:
                gotoAutolockAct();
                break;

            //离线地图
            case R.id.layout_map_offline:
                goToMapOffline();
                break;
            case R.id.layout_battery:
                gotoBattery();
                break;
            case R.id.layout_phoneAlarm:
                gotoPhoneAlarm();
                break;
            default:
                break;
        }
    }

    /**
     * 判断是否有用户
     *
     * @return
     */
    private boolean hasUser() {
        if (AVUser.getCurrentUser() == null) {
            Intent intent = new Intent(m_context, LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return false;
        }
        return true;
    }

    private void gotoPhoneAlarm(){
        if (!SettingManager.getInstance().getPhoneIsAlarm()){
            Intent intent = new Intent(m_context, PhoneAlarmActivity.class);
            startActivity(intent);
        }else {
            Intent intent = new Intent(m_context, PhoneAlarmTestActivity.class);
            startActivity(intent);
        }

    }

    /**
     * 跳转到设备界面
     */
    private void goToDeviceAct() {
        Intent intent = new Intent(m_context, CarManageActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转到离线地图界面
     */
    private void goToMapOffline(){
        Intent intent = new Intent(m_context,MapOfflineActivity.class);
        startActivity(intent);
        //TODO 添加选择是否WIFI环境下自动下载地图的功能
    }
    /**
     * 跳转到个人中心界面
     */
    private void goToPersonCenterAct() {
        Intent intent = new Intent(m_context, PersonalCenterActivity.class);
        startActivity(intent);
    }

    private void gotoAutolockAct(){
        Intent intent = new Intent(m_context, Autolock.class);
        startActivity(intent);
    }

    private void gotoBattery(){
        Intent intent = new Intent(m_context, BatteryTypeActivity.class);
        startActivity(intent);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
// TODO Auto-generated method stub
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            refreshAutolockStatus();
            refreshBatteryStatus();
        }
    }

    /**
     * 退出登录
     */
    private void loginOut() {
        //解析xml
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_logout, null);
        final Dialog dialog = new Dialog(m_context, R.style.Translucent_NoTitle_white);
        Button confirm = (Button) view.findViewById(R.id.dialog_confirm);
        Button cancel = (Button) view.findViewById(R.id.dialog_cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttConnectManager = MqttConnectManager.getInstance();
                mqttConnectManager.unSubscribe(setManager.getIMEI(), new MqttConnectManager.Callback() {
                    @Override
                    public void onSuccess() {
                        Intent intent;
                        intent = new Intent();
                        intent.setAction("com.xunce.electrombile.alarmservice");
                        intent.setPackage(m_context.getPackageName());
                        m_context.stopService(intent);
                        ToastUtils.showShort(m_context, "退出登录成功");
                        setManager.cleanAll();

                        //关闭mqttclient
                        mqttConnectManager.MqttDisconnect();

                        FragmentActivity.cancelAllNotification();
                        intent = new Intent(m_context, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        AVUser.logOut();
                        getActivity().finish();
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showShort(m_context,"退出登录失败，解订阅失败"+e.getMessage());

                    }
                });

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        WindowManager m = m_context.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75); // 宽度设置为屏幕的0.65
        dialog.addContentView(view, new LinearLayout.LayoutParams(dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.show();
    }

    /**
     * 初始化布局
     */
    private void initView(View view) {
        tv_autolockstatus = (TextView)view.findViewById(R.id.tv_autolockstatus);
        tv_batteryType = (TextView)view.findViewById(R.id.tv_batteryType);
        tv_phoneAlarm = (TextView)view.findViewById(R.id.tv_phoneAlarm);
        view.findViewById(R.id.layout_about).setOnClickListener(this);
//        view.findViewById(R.id.layout_help).setOnClickListener(this);
        view.findViewById(R.id.btn_logout).setOnClickListener(this);
        view.findViewById(R.id.layout_person_center).setOnClickListener(this);
        view.findViewById(R.id.rl_1).setOnClickListener(this);
        view.findViewById(R.id.layout_autolock).setOnClickListener(this);
        view.findViewById(R.id.layout_map_offline).setOnClickListener(this);
        view.findViewById(R.id.layout_battery).setOnClickListener(this);
        view.findViewById(R.id.layout_phoneAlarm).setOnClickListener(this);

        View titleView = view.findViewById(R.id.ll_button) ;
        TextView titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        titleTextView.setText("我");
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setVisibility(View.INVISIBLE);

        refreshAutolockStatus();
        refreshBatteryStatus();
        refreshPhoneAlarm();
    }

    public void refreshAutolockStatus(){
        //设置自动落锁的开关状态
        if (null != tv_autolockstatus) {
            if (setManager.getAutoLockStatus()) {
                int period = setManager.getAutoLockTime();
                String s = period + "分钟状态开启";
                tv_autolockstatus.setText(s);
            } else {
                tv_autolockstatus.setText("状态关闭");
            }
        }
    }
    public void refreshBatteryStatus(){
        if (null != tv_batteryType) {
            if (setManager.getBatteryType() != 0) {
                tv_batteryType.setText(setManager.getBatteryType() + "V");
            } else {
                tv_batteryType.setText("未设置");
            }
        }
    }

    public void refreshPhoneAlarm(){
        if (null != tv_phoneAlarm) {
            if (setManager.getPhoneIsAlarm()){
                tv_phoneAlarm.setText("已开通");
            }else {
                tv_phoneAlarm.setText("未开通");
            }
        }
    }

    @Subscribe(priority = 0)
    public void onBatteryTypeEvent(BatteryTypeEvent event){
        refreshBatteryStatus();
    }
    @Subscribe
    public void onPhoneAlarmEvent(PhoneAlarmEvent event){
        refreshPhoneAlarm();
    }
}
