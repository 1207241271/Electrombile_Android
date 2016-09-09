package com.xunce.electrombile.eventbus;

/**
 * Created by yangxu on 16/9/9.
 */
public class QueryItineraryEvent {
    public final double    itinerary;
    public QueryItineraryEvent(double itinerary){
        this.itinerary  =   itinerary;
    }

    public double getItinerary() {
        return itinerary;
    }
}
