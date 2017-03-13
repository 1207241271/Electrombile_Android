package com.xunce.electrombile.manager;

import com.xunce.electrombile.Constants.HttpConstant;
import com.xunce.electrombile.eventbus.http.HttpGetEvent;
import com.xunce.electrombile.eventbus.http.HttpPostEvent;
import com.xunce.electrombile.utils.device.StreamToStringUtil;
import com.xunce.electrombile.utils.device.StringUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yangxu on 2017/3/4.
 */

public class HttpManager {
    public enum getType{
        GET_TYPE_WEATHER,
        GET_TYPE_TODAYITINERARY,
        GET_TYPE_ROUTES,
        GET_TYPE_GPS_POINTS
    }

    public enum postType{
        POST_TYPE_DEVICE,
    }

    public static void getHttpResult(final String url, final getType type){
        new Thread(new Runnable(){
            @Override
            public void run() {
                HttpURLConnection connection;
                try {
                    URL getURL = new URL(url);
                    connection = (HttpURLConnection) getURL.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setRequestProperty("Content-Type","application/json");
                    String result = StreamToStringUtil.StreamToString(connection.getInputStream());
                    EventBus.getDefault().post(new HttpGetEvent(type, StringUtil.decodeUnicode(result),true));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static void postHttpResult(final String url, final postType postType , final HttpConstant.HttpCmd cmd, final String body){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] bytes = body.getBytes();
                HttpURLConnection connection;
                try{
                    URL postURL = new URL(url);
                    connection = (HttpURLConnection) postURL.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type","application/json");
                    connection.setRequestProperty("Content-Length",String.valueOf(bytes.length));
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(bytes);

                    int response = connection.getResponseCode();
                    if (response == HttpURLConnection.HTTP_OK){
                        String result = StreamToStringUtil.StreamToString(connection.getInputStream());
                        EventBus.getDefault().post(new HttpPostEvent(postType,StringUtil.decodeUnicode(result),true,cmd));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
