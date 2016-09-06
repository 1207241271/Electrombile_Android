package com.xunce.electrombile.eventbus;

import java.util.Map;

/**
 * Created by yangxu on 16/9/2.
 */
public class ObjectEvent {
    public final Map eventMap;

    public ObjectEvent(Map newMap){
        this.eventMap = newMap;
    }

    public Map getEventMap() {
        return eventMap;
    }
}
