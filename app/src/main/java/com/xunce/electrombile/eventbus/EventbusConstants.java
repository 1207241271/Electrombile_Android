package com.xunce.electrombile.eventbus;

/**
 * Created by lybvinci on 16/8/11.
 */
public class EventbusConstants {
    public enum  eventBusType{
        EventType_FetchItinerary,           //获取总里程数
        EventType_FenceSet,                 //设置小安宝开关
        EventType_FenceGet,                 //获取小安宝开关开关
        EventType_AutoLockSet,              //设置自动落锁
        EventType_AutoLockGet,              //获取自动落锁
        EventType_AutoPeriodSet,            //设置落锁时间
        EventType_AutoPeriodGet,            //获取落锁时间
        EventType_AutoStatusGet,            //自动落锁状态获取
        EventType_CMDGPSGET                 //CMDGPS数据获得
    }

    public enum carSituationType{
        carSituation_Online,
        carSituation_Waiting,
        carSituation_Offline,
        carSituation_Unkown
    }

    public static final String FromgetHeadImageFromServer = "FromgetHeadImageFromServer";

    public static final String  CancelWaitTimeOut   = "CancelWaitTimeOut";
    public static final String  VALUE   =   "VALUE";
    //----------    online :0   waiting:1   offline:2
    public static final String  carSituation   =   "carSituation";


}
