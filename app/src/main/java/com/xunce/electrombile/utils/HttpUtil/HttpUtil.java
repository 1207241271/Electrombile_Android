package com.xunce.electrombile.utils.HttpUtil;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
}
