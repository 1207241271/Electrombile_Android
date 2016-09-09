package com.xunce.electrombile.eventbus;

import com.xunce.electrombile.manager.TracksManager;

/**
 * Created by yangxu on 16/9/9.
 */
public class GPSEvent {
    public final EventbusConstants.carSituationType      carSituationType;
    public final TracksManager.TrackPoint                trackPoint;
    public GPSEvent(EventbusConstants.carSituationType carSituationType, TracksManager.TrackPoint trackPoint){
        this.carSituationType   =   carSituationType;
        this.trackPoint         =   trackPoint;
    }

    public EventbusConstants.carSituationType getCarSituationType() {
        return carSituationType;
    }

    public TracksManager.TrackPoint getTrackPoint() {
        return trackPoint;
    }
}
