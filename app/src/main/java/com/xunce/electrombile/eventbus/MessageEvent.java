package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/2.
 */
public class MessageEvent {
    public final String mMsg;
    public MessageEvent(String msg) {
        this.mMsg = msg;
    }
    public String getMsg(){
        return mMsg;
    }
}
