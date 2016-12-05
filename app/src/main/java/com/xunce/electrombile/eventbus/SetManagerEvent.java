package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/9.
 */
public class SetManagerEvent {
    public final EventbusConstants.eventBusType    eventBusType;
    public final Object    value;

    public SetManagerEvent(EventbusConstants.eventBusType eventBusType,Object value){
        this.eventBusType   =   eventBusType;
        this.value          =   value;
    }

    public EventbusConstants.eventBusType getEventBusType() {
        return eventBusType;
    }

    public Object getValue() {
        return value;
    }
}
