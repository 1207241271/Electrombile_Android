package com.xunce.electrombile.utils.HttpUtil;

import android.util.Log;

import com.alibaba.fastjson.util.UTF8Decoder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

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
            if (null != charset){
                HttpEntity entity = prepareHttpEntity(charset);

                httpPost.setEntity(entity);
                Header header = new BasicHeader("content-Type","application/json");
                httpPost.setHeader(header);
            }
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200){
                String string = EntityUtils.toString(httpResponse.getEntity());
                result = string;
                Log.w(Tag,string);
            }else {
                return "error";
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
            if (null != charset){
                Header header = new BasicHeader("content-Type","application/json");
                httpDelete.setHeader(header);
            }
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

    public static String sendPut(String url,String charset){
        String result = "";
        HttpPut httpPut = new HttpPut(url);
        try {
            if (null != charset){
                HttpEntity entity = prepareHttpEntity(charset);

                httpPut.setEntity(entity);
                Header header = new BasicHeader("content-Type","application/json");
                httpPut.setHeader(header);
            }

            HttpResponse httpResponse = new DefaultHttpClient().execute(httpPut);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode ==200){
                String string = EntityUtils.toString(httpResponse.getEntity());
                result = string;
            }else {
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    private static HttpEntity prepareHttpEntity(String postData) {

        HttpEntity requestHttpEntity = null;

        try {

            if (null != postData) {
                // 去掉所有的换行
                postData = postData.replace("\n", "");
                // one way
                // requestHttpEntity = new ByteArrayEntity(
                // postData.getBytes(getParamsEncoding()));

                // another way
                requestHttpEntity = new StringEntity(postData,
                        getParamsEncoding());
                ((StringEntity) requestHttpEntity)
                        .setContentEncoding(getParamsEncoding());
                ((StringEntity) requestHttpEntity)
                        .setContentType(getBodyContentType());

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return requestHttpEntity;
    }
    public static String getParamsEncoding() {
        return "UTF-8";
    }

    public static String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset="
                + getParamsEncoding();
    }
}
