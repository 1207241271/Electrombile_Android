package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/10.
 */
public class NotifiyArriviedEvent {
    private final String    date_str;
    public NotifiyArriviedEvent(String date_str){
        this.date_str   =   date_str;
    }

    public String getDate_str() {
        return date_str;
    }
}
