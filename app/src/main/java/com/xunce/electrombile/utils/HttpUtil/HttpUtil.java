package com.xunce.electrombile.utils.HttpUtil;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;

/**
 * Created by yangxu on 2016/10/22.
 */
public class HttpUtil {
    public static final String Tag = "HttpUtil";
    public static String sendGet(String url, String charset){
        String result = "";
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200){
                String string = EntityUtils.toString(httpResponse.getEntity());
                result = string;
                Log.w(Tag,string);
            }else {
                Log.w(Tag,httpResponse.getStatusLine().toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public static String sendPost(String url,String charset){
        String result = "";
        HttpPost httpPost= new HttpPost(url);
        try {
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200){
                String string = EntityUtils.toString(httpResponse.getEntity());
                result = string;
                Log.w(Tag,string);
            }else {
                Log.w(Tag,httpResponse.getStatusLine().toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static String sendDelete(String url,String charset){
        String result = "";
        HttpDelete httpDelete = new HttpDelete(url);
        try {
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpDelete);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200){
                String string = EntityUtils.toString(httpResponse.getEntity());
                result = string;
                Log.w(Tag,string);
            }else {
                Log.w(Tag,httpResponse.getStatusLine().toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
