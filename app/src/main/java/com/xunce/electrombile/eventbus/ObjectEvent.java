package com.xunce.electrombile.eventbus;

import java.util.Map;

/**
 * Created by yangxu on 16/9/2.
 */
public class ObjectEvent {
    public final Map eventMap;
    public final EventbusConstants.objectEventType  eventType;

    //MAP : {value:"the result of set",others}
    public ObjectEvent(Map newMap , EventbusConstants.objectEventType type){
        this.eventMap = newMap;
        eventType = type;
    }

    public Map getEventMap() {
        return eventMap;
    }
    public EventbusConstants.objectEventType getEventType(){
        return eventType;
    }
}
