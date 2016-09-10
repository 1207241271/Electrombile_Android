package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/10.
 */
public class OnlineStatusEvent {
    private final boolean   isOnline;
    public OnlineStatusEvent(boolean isOnline){
        this.isOnline   =   isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }
}
