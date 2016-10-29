package com.xunce.electrombile.manager;

import android.content.Context;
import android.util.Log;

import com.avos.avoscloud.AVObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by heyukun on 2015/4/22.
 */
public class TracksManager implements Serializable{
    private static ArrayList<ArrayList<TrackPoint>> tracks;
    private static ArrayList<ArrayList<ArrayList<TrackPoint>>> GroupTracks;


    private final String TAG = "TracksManager";
    private final String KET_TIME = "createdAt";
    private final String KET_LONG = "lon";
    private final String SPEED = "speed";
    private final String KET_LAT = "lat";
    private final String TIMESTAMP = "timestamp";

    private final long MAX_TIMEINRVAL = 20 * 60;//30分钟
    private final long MAX_DISTANCE = 15;//30分钟
    private CmdCenter mCenter;
    private HashMap<String, ArrayList<ArrayList<TrackPoint>>> map;
    public HashMap<String, ArrayList<Integer>> milesMap;

    public TracksManager(Context context){
        tracks = new ArrayList<ArrayList<TrackPoint>>();
        mCenter = CmdCenter.getInstance();
        map = new HashMap<>();
        milesMap = new HashMap<>();
    }

    public static ArrayList<ArrayList<TrackPoint>> getTracks(){
        return tracks;
    }

    public static void clearTracks(){
        tracks.clear();
    }

    public ArrayList<TrackPoint> getTrack(int position) {
        return tracks.get(position);
    }

    public void setTracksData(ArrayList<ArrayList<TrackPoint>> data) {
        tracks = data;
    }

    public boolean isOutOfHubei(LatLng point){
            return !((point.longitude > 108) && (point.longitude < 116) && (point.latitude > 29) && (point.latitude < 33));
    }

//    //这个函数看的不是很懂啊
    public void setTranks(int groupposition,List<AVObject> objects){
        tracks = new ArrayList<>();
        Log.i("Track managet-----", "setTranks" + objects.size());
        AVObject lastSavedObject = null;
        LatLng lastSavedPoint = null;
        ArrayList<TrackPoint> dataList = null;

        for(AVObject thisObject: objects){
            if(dataList == null){
                dataList = new ArrayList<>();
                tracks.add(dataList);
            }
            double lat = thisObject.getDouble(KET_LAT);
            double lon = thisObject.getDouble(KET_LONG);
            int speed = thisObject.getInt(SPEED);

            //百度地图的LatLng类对输入有限制，如果longitude过大，则会导致结果不正确
            //lybvinci 修改 @date 9.28
            LatLng oldPoint = new LatLng(lat, lon);
            LatLng bdPoint = mCenter.convertPoint(oldPoint);

            //如果本次循环数据跟上一个已保存的数据坐标相同，则跳过
            double dis = Math.abs(DistanceUtil.getDistance(lastSavedPoint, bdPoint));
            Log.i("******", dis + "");
            //如果上次的点和这次的点之间的距离小于200m就不记录了  这样合理吗????
            if(lastSavedObject != null && dis  <= MAX_DISTANCE){
                //Log.i("","distance should less 200M:::" + dis);
                continue;
            }

            //如果下一个数据与上一个已保存的数据时间间隔大于MAX_TIMEINRVAL
            if(lastSavedObject != null &&((thisObject.getCreatedAt().getTime() - lastSavedObject.getCreatedAt().getTime()) / 1000 >= MAX_TIMEINRVAL)){
                Log.e("stilllllll point", "");
//                if(dataList.size() > 1) {
//                    tracks.add(dataList);
//                }
                if(tracks.get(tracks.size() - 1).size() <= 1) {
                    tracks.remove(tracks.size() - 1);
                }
                dataList = new ArrayList<>();
                tracks.add(dataList);
            }

            //打印当前点信息
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            TrackPoint p = new TrackPoint(thisObject.getCreatedAt(), bdPoint,speed);
            //不确定这样处理会不会有什么错误   现在关于日期处理的这个部分还没有搞得很清楚
            p.time.setHours(p.time.getHours());
            dataList.add(p);
            lastSavedObject = thisObject;
            lastSavedPoint = bdPoint;

        }
        //当只有一个列表且列表内只有一个数据时，移除
        if(tracks.size() == 1 && tracks.get(0).size() <= 1){
            tracks.remove(tracks.size() - 1);
        }
        Log.i(TAG, "tracks1 size:" + tracks.size());
        SetMapTrack(groupposition, tracks);
    }

    public void setTracks(int groupPostion, JSONArray array){
        tracks = new ArrayList<>();
        Log.i("Track managet-----","setTracks"+array.length());
        JSONObject lastSavedObject = null;
        LatLng lastSavedPoint = null;
        ArrayList<TrackPoint> pointList = null;

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject object = (JSONObject) array.get(i);

                if (pointList == null) {
                    pointList = new ArrayList<>();
                    tracks.add(pointList);
                }
                double lat = object.getDouble(KET_LAT);
                double lon = object.getDouble(KET_LONG);
                int speed = object.getInt(SPEED);
                LatLng oldPoint = new LatLng(lat, lon);
                LatLng bdPoint = mCenter.convertPoint(oldPoint);
                double dis = Math.abs(DistanceUtil.getDistance(lastSavedPoint, bdPoint));
                Log.i("****", dis + "");
                if (lastSavedObject != null && dis <= MAX_DISTANCE) {
                    continue;
                }
                if (lastSavedObject != null && (object.getInt(TIMESTAMP) - lastSavedObject.getInt(TIMESTAMP) >= MAX_TIMEINRVAL)) {
                    if (tracks.get(tracks.size() - 1).size() <= 1) {
                        tracks.remove(tracks.size() - 1);
                    }
                    pointList = new ArrayList<>();
                    tracks.add(pointList);
                }
                TrackPoint point = new TrackPoint(new Date(object.getLong(TIMESTAMP) * 1000), bdPoint, speed);
                point.time.setHours(point.time.getHours());
                pointList.add(point);
                lastSavedObject = object;
                lastSavedPoint = bdPoint;
            }catch (Exception e){
                e.printStackTrace();
            }
            }
        if (tracks.size() == 1 && tracks.get(0).size() <= 1||tracks.get(tracks.size()-1).size() <= 1){
            tracks.remove(tracks.size() - 1);
        }
        SetMapTrack(groupPostion , tracks);

    }

    public void initTracks(int size){
        tracks = new ArrayList<>(size);
    }

    //添加一段轨迹
    public void setOneTrack(List<AVObject> objects,int position){
        ArrayList<TrackPoint> dataList = new ArrayList<>();

        for(AVObject thisObject: objects){
            double lat = thisObject.getDouble(KET_LAT);
            double lon = thisObject.getDouble(KET_LONG);
            int speed = thisObject.getInt(SPEED);
            //百度地图的LatLng类对输入有限制，如果longitude过大，则会导致结果不正确
            //lybvinci 修改 @date 9.28
            LatLng oldPoint = new LatLng(lat, lon);
            LatLng bdPoint = mCenter.convertPoint(oldPoint);

            TrackPoint p = new TrackPoint(thisObject.getCreatedAt(), bdPoint,speed);

            p.time.setHours(p.time.getHours());
            if(isOutOfHubei(bdPoint)){
                Log.i(TAG, "out range");
                continue;
            }
            dataList.add(p);
        }
        tracks.add(position,dataList);
        Log.d("setOneTrack","1");
    }

    public void orderTracks(){
        int size = tracks.size();
        if(size == 1){
            return;
        }

        long minTimestamp;
        int MinPosition;

        ArrayList<TrackPoint> temp;

        for(int i=0;i<size;i++){
            minTimestamp = tracks.get(i).get(0).time.getTime();
            MinPosition = i;
            for(int j = i+1;j<size;j++){
                if(minTimestamp>tracks.get(j).get(0).time.getTime()){
                    MinPosition = j;
                    minTimestamp = tracks.get(j).get(0).time.getTime();
                }
            }
            if(MinPosition!=i){
                temp = tracks.get(i);
                tracks.set(i,tracks.get(MinPosition));
                tracks.set(MinPosition,temp);
            }
        }
    }



    public static class TrackPoint implements Serializable{
        public Date time;
        public LatLng point;
        public int speed;

        public TrackPoint(Date t, LatLng p) {
            time = t;
            point = p;
        }

        public TrackPoint(Date t, LatLng p,int speed) {
            time = t;
            point = p;
            this.speed = speed;
        }

        public TrackPoint(Date t, double lat, double lon) {
            time = t;
            point = new LatLng(lat, lon);
        }

        public TrackPoint(Date t, double lat, double lon,int speed) {
            time = t;
            point = new LatLng(lat, lon);
            this.speed = speed;
        }
    }

    //groupposition是指某一天
    public void SetMapTrack(int groupposition, ArrayList<ArrayList<TrackPoint>> tracks){
        String grouppositon_str = String.valueOf(groupposition);
        map.put(grouppositon_str, tracks);

        int size =tracks.size();
        Log.d(" tracks_size",String.valueOf(size));
    }

    public void setMilesMap(int groupposition,ArrayList<Integer> milesList){
        String grouppositon_str = String.valueOf(groupposition);
        milesMap.put(grouppositon_str,milesList);
    }

    public HashMap<String, ArrayList<ArrayList<TrackPoint>>> getMapTrack(){
        return map;
    }
}
