package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/9.
 */
public class AutoLockEvent {
    private final EventbusConstants.eventBusType    eventBusType;
    private final boolean   autoLockFlag;
    public AutoLockEvent(boolean autoLockFlag,EventbusConstants.eventBusType    eventBusType){
        this.autoLockFlag   =   autoLockFlag;
        this.eventBusType   =   eventBusType;
    }

    public boolean isAutoLockFlag() {
        return autoLockFlag;
    }

    public EventbusConstants.eventBusType getEventBusType() {
        return eventBusType;
    }
}
