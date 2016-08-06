package com.xunce.electrombile.eventbus;

/**
 * Created by lybvinci on 16/8/6.
 */
public class FirstEvent {
    private String mMsg;
    public FirstEvent(String msg) {
        // TODO Auto-generated constructor stub
        mMsg = msg;
    }
    public String getMsg(){
        return mMsg;
    }
}
