package com.xunce.electrombile.eventbus;

/**
 * Created by lybvinci on 16/8/11.
 */
public class EventbusConstants {
    public static final String FromcaseFence = "FromcaseFence";
    public static final String FromgetHeadImageFromServer = "FromgetHeadImageFromServer";
    public static final String FromcaseFenceGet = "FromcaseFenceGet";
    public static final String FromMyReceiverTimeHandler = "FromMyReceiverTimeHandler";

    public static final String  FetchItineraryEvent = "FetchItineraryEvent";

    public static final String  VALUE   =   "VALUE";
    public enum objectEventType{
        EventType_FetchItinerary,           //获取总里程数
        EventType_FenceSet,                 //设置小安宝开关
        EventType_FenceGet;                 //获取小安宝开关
    }
}
