package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 2016/11/19.
 */

public class PhoneAlarmEvent {
    private boolean isOpen;
    public PhoneAlarmEvent(boolean isOpen){
        this.isOpen = isOpen;
    }

    public boolean isOpen(){
        return isOpen;
    }
}
