/**
 * Project Name:XPGSdkV4AppBase
 * File Name:SettingManager.java
 * Package Name:com.gizwits.framework.sdk
 * Date:2015-1-27 14:47:24
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xunce.electrombile.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.applicatoin.App;
import com.xunce.electrombile.eventbus.EventbusConstants;
import com.xunce.electrombile.eventbus.SetManagerEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * SharePreference处理类.
 *
 * @author lybvinci
 */
public class SettingManager {

    private final String ALARM_FLAG = "alarmFlag";
    /**
     * The share preferences.
     */
    private final String SHARE_PREFERENCES = "set";

    /**
     * The phone num.
     */
    private final String PHONE_NUM = "phoneNumber";

    private final String NICKNAME = "nickname";
    private final String GENDER = "gender";
    private final String BIRTHDATE="birthdate";
    private final String AUTOLOCKSTATUS = "autolockstatus";
    private final String AUTOLOCKTIME = "autolocktime";
    private final String FLAGCARSWITCHED = "flagcarswitched";
    private final String MQTTSTATUS = "MqttStatus";
    private final String IMEI = "imie";

    //用户中心相关
    private final String PHOTO_FLAG = "photoFlag";
    private final String CAR_NUMBER = "carNumber";
    private final String SIM_NUMBER = "simNumber";

    //用户的初始位置
    private final String Lat = "lat";
    private final String Longitude = "longitude";

    private final String MQTTHost = "MQTTHost";

    public final String releaseMQTTHost = "mqtt.xiaoan110.com";
    public final String testMQTTHost = "test.xiaoan110.com";

    private final String MQTTPort = "MQTTPort";
    public final String releaseMQTTPort = "1883";
    public final String testMQTTPort    = "1883";


    private final String UpdateTime = "UpdateTime";

    private final String HttpHost = "HttpHost";

    public final String releaseHttpHost = "http://api.xiaoan110.com:";
    public final String testHttpHost = "http://test.xiaoan110.com:";

    private final String HttpPort = "HttpPort";
    public final String releaseHttpPort = "80";
    public final String testHttpPort = "8081";

    /**
     * The spf.
     */

    SharedPreferences spf;

    private final static SettingManager INSTANCE = new SettingManager();
    public static SettingManager getInstance() {
        return INSTANCE;
    }

    /**
     * Instantiates a new setting manager.
     */
    private SettingManager() {
        Context cc = App.getInstance();
        spf = cc.getSharedPreferences(SHARE_PREFERENCES, Context.MODE_PRIVATE);
        EventBus.getDefault().register(this);
    }

    /**
     * 清理所有信息
     */
    public void cleanAll() {
        setPhoneNumber("");
        setPersonCenterCarNumber("");
        setPersonCenterImage(0);
        setPersonCenterSimNumber("");
        cleanDevice();
    }

    /**
     * 清理设备信息
     */
    public void cleanDevice() {
        setIMEI("");
        setAlarmFlag(false);
        setInitLocation("", "");
    }

    /**
     * 设置初始位置
     *
     * @param lat       纬度
     * @param longitude 经度
     */
    public void setInitLocation(String lat, String longitude) {
        spf.edit().putString(Lat, lat).apply();
        spf.edit().putString(Longitude, longitude).apply();
    }

    /**
     * 获得初始纬度
     * @return
     */
    public String getInitLocationLat() {
        return spf.getString(Lat, "");
    }

    /**
     * 获得初始经度
     * @return
     */
    public String getInitLocationLongitude() {
        return spf.getString(Longitude, "");
    }

    /**
     * 获得报警标志状态
     * @return
     */
    public boolean getAlarmFlag() {
        return spf.getBoolean(ALARM_FLAG, false);
    }

    /**
     * 设置报警状态标志
     * @param alarmFlag
     */
    public void setAlarmFlag(boolean alarmFlag) {
        spf.edit().putBoolean(ALARM_FLAG, alarmFlag).apply();
    }

    /**
     *
     * @return 电话号码
     */
    public String getPhoneNumber() {
        return spf.getString(PHONE_NUM, "");
    }

    /**
     * 设置电话号码
     *
     * @param phoneNumber 电话号码
     */
    public void setPhoneNumber(String phoneNumber) {
        spf.edit().putString(PHONE_NUM, phoneNumber).apply();
    }

    public void setUpdateTime(long millisecond){
        spf.edit().putLong(UpdateTime, millisecond).apply();
    }

    public long getUpdateTime(){
        return spf.getLong(UpdateTime,0);
    }



    //存取IMEI列表  有没有方判断list中哪一个IMEI是正在被监视的???
    public void setIMEIlist(List<String> IMEIlist){
        if(IMEIlist == null){
            spf.edit().putString("IMEILIST", null).apply();
            return;
        }

        JSONArray jsonArray = new JSONArray();
        for (String IMEI : IMEIlist) {
            jsonArray.put(IMEI);
        }
        spf.edit().putString("IMEILIST", jsonArray.toString()).apply();
    }

    public List<String> getIMEIlist(){
        List<String> IMEIlist = new ArrayList<>();
        JSONArray jsonArray;
        try{
            jsonArray = new JSONArray(spf.getString("IMEILIST", null));
            for (int i = 0; i < jsonArray.length(); i++) {
                IMEIlist.add(jsonArray.getString(i));
            }
        }catch (Exception e){
            //这里怎么处理呢?
        }
        return IMEIlist;
    }

    //记录是否是第一次登录
    public void setFirstLogin(Boolean flag){
        spf.edit().putBoolean("FIRSTLOGIN", flag).apply();
    }
    public Boolean getFistLogin(){
        return spf.getBoolean("FIRSTLOGIN", true);
    }




    public void setMqttStatus(Boolean MqttStatus){
        spf.edit().putBoolean(MQTTSTATUS, MqttStatus).apply();
    }

    public Boolean getMqttStatus() {
        return spf.getBoolean(MQTTSTATUS, false);
    }



    public void setAutoLockStatus(Boolean autoLockStatus){
        spf.edit().putBoolean(AUTOLOCKSTATUS, autoLockStatus).apply();
    }

    public Boolean getAutoLockStatus(){
        return spf.getBoolean(AUTOLOCKSTATUS, false);
    }

    public void setAutoLockTime(int autolocktime){
        spf.edit().putInt(AUTOLOCKTIME, autolocktime).apply();
    }

    public int getAutoLockTime(){
        return spf.getInt(AUTOLOCKTIME, 5);
    }

    public void setFlagCarSwitched(String flagCarSwitched){
        spf.edit().putString(FLAGCARSWITCHED, flagCarSwitched).apply();
    }

    //电池电量
    public void setBatteryPercent(int percent){
        spf.edit().putInt("percent", percent).apply();
    }

    public int getBatteryPercent(){
        return spf.getInt("percent",0);
    }

    //获取可行驶公里数
    public void setMiles(int miles){
        spf.edit().putInt("miles", miles).apply();
    }

    public int getMiles(){
        return spf.getInt("miles",0);
    }

    public void setBatteryType(int type){ spf.edit().putInt("batteryType",type).apply();}

    public int getBatteryType(){return spf.getInt("batteryType",0);}

    public String getFlagCarSwitched(){
        return spf.getString(FLAGCARSWITCHED, "没有切换");
    }

    /**
     *
     * @return 获取IMEI号
     */
    public String getIMEI() {
        return spf.getString(IMEI, "");
    }

    /**
     * 设置IMEI号
     * @param did  IMEI号
     */
    public void setIMEI(String did) {
        spf.edit().putString(IMEI, did).apply();
    }



    /**
     *
     * @return 获取MQTT Service
     */
    public String getMQTTHost() {
        return spf.getString(MQTTHost, releaseMQTTHost);
    }

    /**
     *
     * @param mqttHost MQTT服务
     */
    public void setMQTTHost(String mqttHost) {
        spf.edit().putString(MQTTHost, mqttHost).apply();
    }


    public void setMQTTPort(String mqttPort){
        spf.edit().putString(MQTTPort,releaseMQTTPort).apply();
    }

    public String getMQTTPort(){
        return spf.getString(MQTTPort,releaseMQTTPort);
    }
    /********************************个人中心相关****************************************/

    /**
     * 获取个人中心已经设置的图片个数
     *
     * @return
     */
    public int getPersonCenterImage() {
        return spf.getInt(PHOTO_FLAG, 0);
    }

    /**
     * 设置个人中心设置的图片个数
     *
     * @param did
     */
    public void setPersonCenterImage(int did) {
        spf.edit().putInt(PHOTO_FLAG, did).apply();
    }

    /**
     * 取出个人中心车辆牌照
     *
     * @return
     */
    public String getPersonCenterCarNumber() {
        return spf.getString(CAR_NUMBER, "");
    }

    /**
     * 保存个人中心车辆牌照
     *
     * @param carNumber
     */
    public void setPersonCenterCarNumber(String carNumber) {
        spf.edit().putString(CAR_NUMBER, carNumber).apply();
    }

    /**
     * 取出个人中心SIM卡号
     *
     * @return
     */
    public String getPersonCenterSimNumber() {
        return spf.getString(SIM_NUMBER, "");
    }

    /**
     * 保存个人中心SIM卡号
     *
     * @param simNumber
     */
    public void setPersonCenterSimNumber(String simNumber) {
        spf.edit().putString(SIM_NUMBER, simNumber).apply();
    }

    //用户昵称
    public void setNickname(String nickname) {
        spf.edit().putString(NICKNAME, nickname).apply();
    }
    public String getNickname() {
        return spf.getString(NICKNAME, "test");
    }


    //性别
    public void setGender(Boolean gender){
        spf.edit().putBoolean(GENDER, gender).apply();
    }

    public Boolean getGender(){
        return spf.getBoolean(GENDER, true);
    }


    //出生日期
    public void setBirthDate(String birthDate){
        spf.edit().putString(BIRTHDATE, birthDate).apply();
    }

    public String getBirthdate(){
        return spf.getString(BIRTHDATE, "1994-01-01");
    }

    /********************************以上为个人中心相关****************************************/

    //每个IMEI都会对应一个json数据 里面包含设备的昵称和设备的绑定日期
    public void setCarName(String IMEI,String carName){
        spf.edit().putString(IMEI+"carname", carName).apply();
    }

    public String getCarName(String IMEI){
        return spf.getString(IMEI+"carname",null);
    }

    public void setCreateTime(String IMEI,String CreateTime){
        spf.edit().putString(IMEI+"createtime", CreateTime).apply();
    }

    public String getCreateTime(String IMEI){
        return spf.getString(IMEI+"createtime",null);
    }

    public void removeKey(String IMEI){
        spf.edit().remove(IMEI+"carname");
        spf.edit().remove(IMEI + "createtime");
    }


    //控制是否需要导航
    public void setNeedGuide(Boolean needGuide){
        spf.edit().putBoolean("NEEDGUIDE", needGuide).apply();
    }

    public Boolean getNeedGuide(){
        return spf.getBoolean("NEEDGUIDE",true);
    }

    //记录上次清理数据库的时间
    public void setUpdateDatabaseTime(long timestamp){
        spf.edit().putLong("UPDATEDATABASETIME", timestamp).apply();
    }

    public long getUpdateDatabaseTime(){
        return spf.getLong("UPDATEDATABASETIME",0);
    }

    /**
     *
     * @param httpHost http服务
     */
    public void setHttpHost(String httpHost){
        spf.edit().putString(HttpHost,httpHost).apply();
    }

    /**
     *
     *@return HttpHost
     */
    public String getHttpHost(){
        return spf.getString(HttpHost,releaseHttpHost);
    }

    public void setHttpPort(String httpPort){
        spf.edit().putString(HttpPort,httpPort).apply();
    }

    public String getHttpPort(){
        return spf.getString(HttpPort,releaseHttpPort);
    }

    public void setHasContracter(Boolean isHave){
        spf.edit().putBoolean("contract",true).apply();
    }

    public boolean getHasContracter(){
       return spf.getBoolean("contract",false);
    }

    public void setPhoneIsAgree(Boolean isAgree){
        spf.edit().putBoolean("phoneAlarm",isAgree).apply();
    }

    public boolean getPhoneIsAlarm(){
        return spf.getBoolean("phoneAlarm",false);
    }

    @Subscribe
    public void onSetManagerEvent(SetManagerEvent event){
        switch (event.getEventBusType()){
            case EventType_FenceGet:
            case EventType_FenceSet:
                boolean alarmFlag = Boolean.parseBoolean(event.getValue().toString());
                this.setAlarmFlag(alarmFlag);
                break;
            case EventType_AutoLockGet:
            case EventType_AutoLockSet:
                boolean autoLockFlag = Boolean.parseBoolean(event.getValue().toString());
                this.setAutoLockStatus(autoLockFlag);
                break;
        }
    }
}
