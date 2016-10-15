package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/9.
 */
public class FenceEvent {
    public final boolean     alarmFlag;
    public final EventbusConstants.eventBusType    eventBusType;
    public FenceEvent(EventbusConstants.eventBusType eventBusType,boolean alarmFlag){
        this.alarmFlag = alarmFlag;
        this.eventBusType = eventBusType;
    }

    public boolean isAlarmFlag() {
        return alarmFlag;
    }

    public EventbusConstants.eventBusType getEventBusType() {
        return eventBusType;
    }
}
