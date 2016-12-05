package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/10.
 */
public class RefreshThreadEvent {
    private final boolean   stopflag;
    public RefreshThreadEvent(boolean stopflag){
        this.stopflag    =   stopflag;
    }

    public boolean isStopflag() {
        return stopflag;
    }
}
