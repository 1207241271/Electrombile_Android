package com.xunce.electrombile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.avos.avoscloud.LogUtil;
import com.baidu.mapapi.model.LatLng;
import com.orhanobut.logger.Logger;
import com.xunce.electrombile.Constants.ActivityConstants;
import com.xunce.electrombile.Constants.ProtocolConstants;
import com.xunce.electrombile.activity.Autolock;
import com.xunce.electrombile.activity.FragmentActivity;
import com.xunce.electrombile.eventbus.AutoLockEvent;
import com.xunce.electrombile.eventbus.BatteryInfoEvent;
import com.xunce.electrombile.eventbus.BatteryTypeEvent;
import com.xunce.electrombile.eventbus.EventbusConstants;
import com.xunce.electrombile.eventbus.FenceEvent;
import com.xunce.electrombile.eventbus.GPSEvent;
import com.xunce.electrombile.eventbus.MessageEvent;
import com.xunce.electrombile.eventbus.NotifiyArriviedEvent;
import com.xunce.electrombile.eventbus.ObjectEvent;
import com.xunce.electrombile.eventbus.OnlineStatusEvent;
import com.xunce.electrombile.eventbus.RefreshThreadEvent;
import com.xunce.electrombile.eventbus.SetManagerEvent;
import com.xunce.electrombile.fragment.SwitchFragment;
import com.xunce.electrombile.log.MyLog;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.manager.TracksManager;
import com.xunce.electrombile.protocol.CmdFactory;
import com.xunce.electrombile.protocol.GPSFactory;
import com.xunce.electrombile.protocol.NotifyFactory;
import com.xunce.electrombile.protocol.NotifyProtocol;
import com.xunce.electrombile.protocol.Protocol;
import com.xunce.electrombile.protocol.ProtocolFactoryInterface;
import com.xunce.electrombile.protocol._433Factory;
import com.xunce.electrombile.utils.system.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lybvinci on 2015/10/22.
 */
public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    private Context mContext;
    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ToastUtils.showShort(mContext, "设备超时！");
            EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
//            ((FragmentActivity)mContext).cancelWaitTimeOut();
        }
    };


    private byte select = 0;
    private Protocol protocol;
    private SettingManager settingManager;

    public MyReceiver(Context context) {
        mContext = context;
        settingManager = SettingManager.getInstance();
    }

    private Protocol createFactory(byte msg, String jsonString) {
        ProtocolFactoryInterface factory;
        Protocol protocol = null;
        switch (msg) {
            case 0x01:
                LogUtil.log.d("收到CMD");
                factory = new CmdFactory();
                protocol = factory.createProtocol(jsonString);
                break;
            case 0x02:
                LogUtil.log.d("收到GPS");
                factory = new GPSFactory();
                protocol = factory.createProtocol(jsonString);
                break;
            case 0x03:
                LogUtil.log.d("收到找车信息");
                factory = new _433Factory();
                protocol = factory.createProtocol(jsonString);
                break;
            case 0x05:
                LogUtil.log.d("收到notify");
                factory = new NotifyFactory();
                protocol = factory.createProtocol(jsonString);
                break;

            default:
                break;
        }
        return protocol;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.i(TAG, "接收调用");
        Logger.i("接收调用%s",intent.getExtras().toString());
        //Log.i(TAG, intent.getExtras().toString());
        Bundle bundle = intent.getExtras();
        String callbackStatus = bundle.get(ActivityConstants.callbackStatus).toString();
        String callbackAction = bundle.get(ActivityConstants.callbackAction).toString();
        if (ActivityConstants.OK.equals(callbackStatus)) {
            if (callbackAction.equals(ActivityConstants.messageArrived)) {
                String destinationName = bundle.get(ActivityConstants.destinationName).toString();
                String s = bundle.get(ActivityConstants.PARCEL).toString();
                if (destinationName.contains("cmd")) {
                    select = 0x01;
                    protocol = createFactory(select, s);
                    Log.i(TAG, "得到命令字");
                    Logger.i(TAG,"得到命令字");
                    onCmdArrived(protocol);
                } else if (destinationName.contains("gps")) {
                    select = 0x02;
                    protocol = createFactory(select, s);
                    Log.i(TAG, "得到GPS");
                    ((FragmentActivity) mContext).cancelWaitTimeOut();
                     onGPSArrived(protocol);
                } else if (destinationName.contains("433")) {
                    select = 0x03;
                    protocol = createFactory(select, s);
                    Log.i(TAG, "433找车");
                    on433Arrived(protocol);
                } else if (destinationName.contains("notify")) {
                    select = 0x05;
                    protocol = createFactory(select, s);
                    MyLog.d(TAG, "notify");
                    onNotifyArrived(protocol);
                }

            } else if (callbackAction.equals(ActivityConstants.onConnectionLost)) {
//                ToastUtils.showShort(mContext, "服务器连接已断开");
                Logger.wtf("服务器连接已断开");
            }
        }
    }


    private void on433Arrived(Protocol protocol) {
        int intensity = protocol.getIntensity();
        caseSeekSendToFindAct(intensity);
    }

    private void onNotifyArrived(Protocol protocol){
        MyLog.d("onNotifyArrived", "start");
        int notify = protocol.getNotify();
        switch (notify){
            //上报的自动落锁状态
            case 1:
                NotifyProtocol.NotifyAutolockData notifyAutolockData = protocol.getData();
                if(notifyAutolockData!=null){
                    long timestamp = notifyAutolockData.Timestamp;
                    Date date = new Date(timestamp*1000);
                    SimpleDateFormat sdfWithSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date_str = sdfWithSecond.format(date);

                    EventBus.getDefault().post(new SetManagerEvent(EventbusConstants.eventBusType.EventType_FenceGet,true));
                    EventBus.getDefault().post(new NotifiyArriviedEvent(date_str));
//                    ((FragmentActivity) mContext).setManager.setAlarmFlag(true);
//                    ((FragmentActivity) mContext).switchFragment.openStateAlarmBtn();
//                    ((FragmentActivity) mContext).switchFragment.showNotification(date_str+"自动落锁成功",
//                            FragmentActivity.NOTIFICATION_AUTOLOCKSTATUS);
                }
                break;

            default:
                break;
        }
    }

    private void onCmdArrived(Protocol protocol) {
        int cmd = protocol.getCmd();
        int code = protocol.getCode();
        int result = protocol.getResult();
        timeHandler.removeMessages(ProtocolConstants.TIME_OUT);
        //----------    check code is Success
            switch (cmd) {
                //lf cmd is turn on switch
                case ProtocolConstants.CMD_FENCE_ON:
                    caseFenceSet(code,true, "防盗开启成功");
                    break;

                //lf cmd is turn off switch
                case ProtocolConstants.CMD_FENCE_OFF:
                    caseFenceSet(code,false, "防盗关闭成功");
                    break;

                //lf cmd is check switch
                case ProtocolConstants.CMD_FENCE_GET:
                    caseFenceGet(code, protocol);
                    break;

                //如果是开始找车的命令
                case ProtocolConstants.CMD_SEEK_ON:
                    caseSeek(code, "开始找车");
                    break;

                //如果是停止找车的命令
                case ProtocolConstants.CMD_SEEK_OFF:
                    caseSeek(code, "停止找车");
                    break;

                case ProtocolConstants.CMD_LOCATION:
                    caseGetGPS(code, protocol);
                    break;

                case ProtocolConstants.APP_CMD_AUTO_LOCK_ON:

                    //开启自动落锁
                    caseOpenAutoLock(code);
                    break;

                case ProtocolConstants.APP_CMD_AUTO_LOCK_OFF:
                    caseCloseAutoLock(code);
                    break;

                case ProtocolConstants.APP_CMD_AUTO_PERIOD_GET:
                    caseGetAutolockPeriod(code, protocol);
                    break;

                case ProtocolConstants.APP_CMD_AUTO_PERIOD_SET:
                    caseSetAutoLockTime(code);
                    break;

                //获取自动落锁的状态
                case ProtocolConstants.APP_CMD_AUTOLOCK_GET:
                    caseGetAutoLockStatus(code, protocol);
                    break;

                //查询电量
                case ProtocolConstants.APP_CMD_BATTERY:
                    caseGetBatteryInfo(code, protocol);
                    break;

                case ProtocolConstants.APP_CMD_STATUS_GET:
                    caseGetInitialStatus(code, protocol);
                    break;

                case ProtocolConstants.APP_CMD_SET_BATTERY_TYPE:

                default:
                    break;
            }
    }

    private void caseBatteryType(int code){
        if(code == 0){
            //自动落锁时间成功设置之后  把时间写到本地
            ToastUtils.showShort(mContext, "电池类型设置成功");
            EventBus.getDefault().post(new BatteryTypeEvent());
            return;
        }
        dealErr(code);
    }

    //这个函数是主动查询gps的时候执行的函数 后面那个服务器主动上报用的
    private void caseGetGPS(int code,Protocol protocol) {
        switch (code) {
            case ProtocolConstants.ERR_SUCCESS:
                sendGPSData(protocol,code);
                EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
                break;
            case ProtocolConstants.ERR_WAITING:
                sendGPSData(protocol,code);
                break;
            case ProtocolConstants.ERR_OFFLINE:
                sendGPSData(protocol,code);
                ToastUtils.showShort(mContext, "设备离线，请确认车辆未处于地下室等信号较差区域");
                EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
                break;
        }
    }
    private void dealErr(int code) {
        switch (code) {
            case ProtocolConstants.ERR_WAITING:
                ToastUtils.showShort(mContext, "正在设置命令，请稍后...");
                timeHandler.sendEmptyMessageDelayed(ProtocolConstants.TIME_OUT, ProtocolConstants.TIME_OUT_VALUE * 2);
                return;
            case ProtocolConstants.ERR_OFFLINE:
                ToastUtils.showShort(mContext, "设备离线，请确认车辆未处于地下室等信号较差区域");
                EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
                //----------    set Flag
                EventBus.getDefault().post(new SetManagerEvent(EventbusConstants.eventBusType.EventType_FenceGet,false));
                break;
            case ProtocolConstants.ERR_INTERNAL:
                ToastUtils.showShort(mContext, "服务器内部错误，请稍后再试。");
                EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
                break;
        }
    }

    private void caseOpenAutoLock(int code){
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        //执行fragmentactivity中的函数
        if(0 == code){
            //默认是自动落锁5分钟
            EventBus.getDefault().post(new AutoLockEvent(true, EventbusConstants.eventBusType.EventType_AutoLockGet));
            return;
        }
        dealErr(code);
    }

    private void caseCloseAutoLock(int code){
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        if(0 == code){
            ToastUtils.showShort(mContext, "自动落锁关闭");
            EventBus.getDefault().post(new AutoLockEvent(false, EventbusConstants.eventBusType.EventType_AutoLockGet));
            return;
        }
        dealErr(code);
    }

    private void caseGetAutolockPeriod(int code,Protocol protocol) {
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        if (0 == code) {
            int period = protocol.getPeriod();
            EventBus.getDefault().post(new SetManagerEvent(EventbusConstants.eventBusType.EventType_AutoPeriodGet,period));
            return;
        }
        dealErr(code);
    }

    public void caseSetAutoLockTime(int code){
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        if(code == 0){
           //自动落锁时间成功设置之后  把时间写到本地
            ToastUtils.showShort(mContext, "自动落锁成功");
            EventBus.getDefault().post(new SetManagerEvent(EventbusConstants.eventBusType.EventType_AutoPeriodSet,Autolock.period));
            return;
        }
        dealErr(code);
    }

    public void caseGetAutoLockStatus(int code,Protocol protocol){
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        if(code == 0){
            //已经获取到了自动落锁的状态
            int state = protocol.getNewState();
            if(state == 1){
                ToastUtils.showShort(mContext, "自动落锁为打开状态");
                EventBus.getDefault().post(new SetManagerEvent(EventbusConstants.eventBusType.EventType_AutoLockGet,true));
                //若为打开状态  还要查询到自动落锁的时间
                EventBus.getDefault().post(new AutoLockEvent(true, EventbusConstants.eventBusType.EventType_AutoStatusGet));
            }
            else if(state == 0){
                ToastUtils.showShort(mContext, "自动落锁为关闭状态");
                EventBus.getDefault().post(new SetManagerEvent(EventbusConstants.eventBusType.EventType_AutoLockGet,false));
            }
            return;
        }
        dealErr(code);
    }

    private void caseGetInitialStatus(int code,Protocol protocol){
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        if(code == 0){
            TracksManager.TrackPoint trackPoint = protocol.getInitialStatusResult();
            if(trackPoint!=null){
                ToastUtils.showShort(mContext, "设备状态查询成功");
                Date date = trackPoint.time;
                CmdCenter mCenter = CmdCenter.getInstance();
                LatLng bdPoint = mCenter.convertPoint(trackPoint.point);
                trackPoint = new TracksManager.TrackPoint(date,bdPoint);

                EventBus.getDefault().post(new GPSEvent(EventbusConstants.carSituationType.carSituation_Online,trackPoint,true));

                //发送广播吧
                Intent intent = new Intent("com.app.bc.test");
                intent.putExtra("KIND","GETINITIALSTATUS");
                mContext.sendBroadcast(intent);//发送广播事件

                EventBus.getDefault().post(new RefreshThreadEvent(false));
            }
        }
        else{
            if (code == ProtocolConstants.ERR_OFFLINE){
                EventBus.getDefault().post(new RefreshThreadEvent(true));
                EventBus.getDefault().post(new OnlineStatusEvent(false));
            }
            dealErr(code);
        }
    }

    private void caseGetBatteryInfo(int code,Protocol protocol){
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        if(code == 0){
            if(!protocol.getBatteryInfo()){
                ToastUtils.showShort(mContext,"获取电量失败");
            }
            else{
                ToastUtils.showShort(mContext,"获取电量成功");
                //这个时候有可能会出现switchFragment的view没有渲染好的情况
                EventBus.getDefault().post(new BatteryInfoEvent());
            }
        }
        else{
            if (code == ProtocolConstants.ERR_OFFLINE){
                EventBus.getDefault().post(new RefreshThreadEvent(true));
            }
            dealErr(code);
        }
    }


    private void caseSeek(int code, String success) {
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        if (ProtocolConstants.ERR_SUCCESS == code) {
            ToastUtils.showShort(mContext, success);
        } else {
            dealErr(code);
        }
        caseSeekSendToFindAct(0);
    }

    private void caseSeekSendToFindAct(int value) {
        if (value == -1) {
            return;
        }
        Intent intent7 = new Intent();
        intent7.putExtra("intensity", value);
        intent7.setAction("com.xunce.electrombile.find");
        mContext.sendBroadcast(intent7);
    }

    private void caseFenceGet(int code,Protocol protocol) {
        if (code == ProtocolConstants.ERR_SUCCESS) {
            int state = protocol.getNewState();
            boolean alarmFlag = false;
            if (ProtocolConstants.ON == state) {
                alarmFlag = true;
            }
            //----------    cancel  wait time out
            EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
            //----------    destination is FragmentActivity And SwitchFragment
            EventBus.getDefault().post(new SetManagerEvent(EventbusConstants.eventBusType.EventType_FenceGet,alarmFlag));
            EventBus.getDefault().post(new FenceEvent(EventbusConstants.eventBusType.EventType_FenceGet,alarmFlag));
            ToastUtils.showShort(mContext, "查询小安宝开关状态成功");
        }else {
            dealErr(code);
        }
    }

    private void caseFenceSet(int code,boolean alarmFlag, String tip) {
        if (code == ProtocolConstants.ERR_SUCCESS) {
            //----------    cancel  wait time out
            EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
            //----------    destination is FragmentActivity And SwitchFragment
            EventBus.getDefault().post(new SetManagerEvent(EventbusConstants.eventBusType.EventType_AutoLockSet,alarmFlag));
            EventBus.getDefault().post(new FenceEvent(EventbusConstants.eventBusType.EventType_FenceSet,alarmFlag));
            ToastUtils.showShort(mContext, tip);
        }else {
            dealErr(code);
        }
    }



    private void onGPSArrived(Protocol protocol) {
        EventBus.getDefault().post(new MessageEvent(EventbusConstants.CancelWaitTimeOut));
        float Flat = protocol.getLat();
        float Flong = protocol.getLng();
        if (Flat == -1 || Flong == -1) {
            return;
        }

        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        TracksManager.TrackPoint trackPoint = null;
        trackPoint = new TracksManager.TrackPoint(curDate,Flat,Flong);
        timeHandler.removeMessages(ProtocolConstants.TIME_OUT);
        LogUtil.log.i("保存数据1");
        LogUtil.log.i("onGPSArrived-locateMobile");
        EventBus.getDefault().post(new GPSEvent(EventbusConstants.carSituationType.carSituation_Online,trackPoint,false));
//                new TracksManager.TrackPoint(curDate, ((FragmentActivity) mContext).mCenter.convertPoint(new LatLng(Flat, Flong)));
        ((FragmentActivity) mContext).setManager.setInitLocation(Flat + "", Flong + "");
        ((FragmentActivity) mContext).maptabFragment.locateMobile(trackPoint);
    }

    private void cmdGPSgetresult(Protocol protocol,int code){
        sendGPSData(protocol,code);
//        TracksManager.TrackPoint trackPoint = protocol.getNewResult();
//        if(trackPoint!=null){
//            Date date = trackPoint.time;
//            CmdCenter mCenter = CmdCenter.getInstance();
//            LatLng bdPoint = mCenter.convertPoint(trackPoint.point);
//            trackPoint = new TracksManager.TrackPoint(date,bdPoint);
//            ((FragmentActivity) mContext).maptabFragment.locateMobile(trackPoint);
//            if(((FragmentActivity) mContext).maptabFragment.LostCarSituation){
//                if(code == 101){
//                    ((FragmentActivity) mContext).maptabFragment.caseLostCarSituationWaiting();
//                }
//                else if(code == 0){
//                    ((FragmentActivity) mContext).maptabFragment.caseLostCarSituationSuccess();
//                }
//            }
//        }
    }

    private void sendGPSData(Protocol protocol,int code){
        TracksManager.TrackPoint    trackPoint  =   protocol.getNewResult();
        if (trackPoint != null){
            //----------    check is curLocate
            EventbusConstants.carSituationType carSituatuin   =   EventbusConstants.carSituationType.carSituation_Unkown;
            switch (code){
                case ProtocolConstants.ERR_SUCCESS:
                    carSituatuin = EventbusConstants.carSituationType.carSituation_Online;
                    break;
                case ProtocolConstants.ERR_WAITING:
                    carSituatuin = EventbusConstants.carSituationType.carSituation_Waiting;
                    break;
                case ProtocolConstants.ERR_OFFLINE:{
                    carSituatuin = EventbusConstants.carSituationType.carSituation_Offline;
                    break;
                }
            }
            //----------    set point data
            Date    date    =   trackPoint.time;
            CmdCenter   cmdCenter   =   CmdCenter.getInstance();
            LatLng  bdPoint =   cmdCenter.convertPoint(trackPoint.point);
            trackPoint  =   new TracksManager.TrackPoint(date,bdPoint);
            //----------    set EventObject
            EventBus.getDefault().post(new GPSEvent(carSituatuin, trackPoint,true));
        }
    }
}