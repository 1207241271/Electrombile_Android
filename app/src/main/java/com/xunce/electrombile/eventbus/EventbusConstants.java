package com.xunce.electrombile.eventbus;

/**
 * Created by lybvinci on 16/8/11.
 */
public class EventbusConstants {
    public enum  objectEventBusType{
        EventType_FetchItinerary,           //获取总里程数
        EventType_FenceSet,                 //设置小安宝开关
        EventType_FenceGet,                 //获取小安宝开关
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
