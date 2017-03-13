package com.xunce.electrombile.Constants;

/**
 * Created by yangxu on 2016/11/13.
 */

public class HttpConstant {
    public static final String ITINERARY = "itinerary";
    public static final String ROUTEMILE = "miles";

    public enum HttpCmd{
        HTTP_CMD_GET_STATUS,
        HTTP_CMD_GET_GPS,
        HTTP_CMD_SET_AUTOLOCK,
        HTTP_CMD_GET_AUTOLOCK,
        HTTP_CMD_SET_FENCE,
        HTTP_CMD_GET_FENCE,
        HTTP_CMD_GET_BATTERY,
        HTTP_CMD_SET_BATTERY_TYPE,
        HTTP_CMD_START_RECORD,
        HTTP_CMD_STOP_RECORD,
        HTTP_CMD_SET_REMOTE_LOCK,
        HTTP_CMD_GET_REMOTE_LOCK,
        HTTP_CMD_SET_RELEVANCE,
        HTTP_CMD_GET_RELEVANCE,
        HTTP_CMD_DEFAULT
    }
}
