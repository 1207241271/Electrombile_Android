package com.xunce.electrombile.eventbus.http;

import com.xunce.electrombile.manager.HttpManager;

/**
 * Created by yangxu on 2016/11/7.
 */

public class HttpGetEvent {
    protected HttpManager.getType      type;
    protected String resultStr;
    protected boolean isSuccess;

    public HttpGetEvent(HttpManager.getType type, String result, boolean isSuccess){
        this.type = type;
        this.resultStr = result;
        this.isSuccess = isSuccess;
    }

    public String getResult() {
        return resultStr;
    }

    public HttpManager.getType getRequestType() {
        return type;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
