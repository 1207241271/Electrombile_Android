package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/9.
 */
public class AutoLockEvent {
    private final boolean   autoLockFlag;
    public AutoLockEvent(boolean autoLockFlag){
        this.autoLockFlag   =   autoLockFlag;
    }

    public boolean isAutoLockFlag() {
        return autoLockFlag;
    }
}
