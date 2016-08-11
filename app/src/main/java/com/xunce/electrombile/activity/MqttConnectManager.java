package com.xunce.electrombile.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Callback;
import com.xunce.electrombile.Constants.ServiceConstants;
import com.xunce.electrombile.R;
import com.xunce.electrombile.applicatoin.App;
import com.xunce.electrombile.eventbus.EventbusConstants;
import com.xunce.electrombile.eventbus.FirstEvent;
import com.xunce.electrombile.log.MyLog;
import com.xunce.electrombile.mqtt.ActionListener;
import com.xunce.electrombile.mqtt.Connection;
import com.xunce.electrombile.mqtt.Connections;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;


//使用单例模式
/**
 * Created by lybvinci on 16/1/21.
 */
public class MqttConnectManager {
    Context mcontext;
    public MqttAndroidClient mac;
    MqttConnectOptions mcp;
    Connection connection;
    OnMqttConnectListener onMqttConnectListener;

    public static final String OK = "OK";
    public static final String LOST = "LOST";
    public static final String IS_CONNECTING = "IS_CONNECTING";
    public static final String CONNECTING_FAIL = "CONNECTING_FAIL";
    public static String status = OK;

//    final Handler handler = new Handler( ) {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 0:
//                   reconnectMqtt(new OnMqttConnectListener() {
//                       @Override
//                       public void MqttConnectSuccess() {
//
//                       }
//
//                       @Override
//                       public void MqttConnectFail() {
////                           handler.sendMessageDelayed(handler.obtainMessage(0), 5000);
//                       }
//                   });
//                    break;
//            }
//        }
//    };

    private MqttConnectManager() {

    }

    private final static MqttConnectManager INSTANCE = new MqttConnectManager();

    public static MqttConnectManager getInstance() {
        return INSTANCE;
    }

    //    获取到对象之后首先执行这个函数
    public void setContext(Context context) {
        mcontext = context;
    }

//    private Connection getConnection(){
//        Connections connections = Connections.getInstance(mcontext);
//        String uri = "tcp://" + ServiceConstants.MQTT_HOST + ":" + ServiceConstants.PORT;
//        String handle = uri + ServiceConstants.clientId;
//        connection = connections.getConnection(handle);
//        if(connection == null){
//            connection = Connection.createConnection(ServiceConstants.clientId,
//                    ServiceConstants.MQTT_HOST,
//                    ServiceConstants.PORT,
//                    mcontext,
//                    false);
//            mcp = new MqttConnectOptions();
//        /*
//         * true :那么在客户机建立连接时，将除去客户机的任何旧预订。当客户机断开连接时，会除去客户机在会话期间创建的任何新预订。
//         * false:那么客户机创建的任何预订都会被添加至客户机在连接之前就已存在的所有预订。当客户机断开连接时，所有预订仍保持活动状态。
//         * 简单来讲，true的话就是每次连接都要重新订阅，false的话就是不用重新订阅
//         */
//            mcp.setCleanSession(false);
//            connection.addConnectionOptions(mcp);
//            connections.addConnection(connection);
//            return connection;
//        }else{
//            return connection;
//        }
//    }

    public void initMqtt() {
        Connections connections = Connections.getInstance(mcontext);

        String uri = "tcp://" + ServiceConstants.MQTT_HOST + ":" + ServiceConstants.PORT;
        final String handle = uri + ServiceConstants.clientId;
        connection = connections.getConnection(handle);
        if (connection == null) {
            connection = Connection.createConnection(ServiceConstants.clientId,
                    ServiceConstants.MQTT_HOST,
                    ServiceConstants.PORT,
                    mcontext,
                    false);
            mcp = new MqttConnectOptions();
        /*
         * true :那么在客户机建立连接时，将除去客户机的任何旧预订。当客户机断开连接时，会除去客户机在会话期间创建的任何新预订。
         * false:那么客户机创建的任何预订都会被添加至客户机在连接之前就已存在的所有预订。当客户机断开连接时，所有预订仍保持活动状态。
         * 简单来讲，true的话就是每次连接都要重新订阅，false的话就是不用重新订阅
         */
            mcp.setCleanSession(false);
            connection.addConnectionOptions(mcp);
            connections.addConnection(connection);
        }
        ServiceConstants.handler = connection.handle();
        mac = connection.getClient();
        //设置监听函数
        mac.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

                if (throwable != null) {
                    Connection c = Connections.getInstance(mcontext).getConnection(connection.handle());
                    c.addAction("Connection Lost");
                    c.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);

                    //format string to use a notification text
                    Object[] args = new Object[2];
                    args[0] = c.getId();
                    args[1] = c.getHostName();

                    String message = mcontext.getString(R.string.connection_lost, args);

                    //build intent
                    Intent intent = new Intent();
                    intent.setClassName(mcontext, "org.eclipse.paho.android.service.sample.ConnectionDetails");
                    intent.putExtra("handle", connection.handle());

                    //notify the user
//                    Notify.notifcation(mcontext, message, intent, R.string.notifyTitle_connectionLost);

                    ServiceConstants.connection_status = "connection lost";
                    Log.d("initMqtt","connection lost");

                    reconnectMqtt(new OnMqttConnectListener() {
                        @Override
                        public void MqttConnectSuccess() {
                            Log.d("connectlost-reconnect","MqttConnectSuccess");
                        }

                        @Override
                        public void MqttConnectFail() {
                            Log.d("connectlost-reconnect","MqttConnectFail");
//                            handler.sendMessageDelayed(handler.obtainMessage(0), 5000);
                        }
                    });
                }
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
//                com.orhanobut.logger.Logger.i("收到MQTT服务器的消息：" + s);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                //publish后会执行到这里
            }
        });
    }

    public void reconnectMqtt(final OnMqttConnectListener callback) {
        if(NetworkUtils.isNetworkConnected(App.getInstance())){
            Connection c = Connections.getInstance(mcontext).getConnection(connection.handle());
            try {
                c.getClient().connect(connection.getConnectionOptions(), this, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        ServiceConstants.connection_status = "connected";
                        connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTED);
                        callback.MqttConnectSuccess();
                        ToastUtils.showShort(App.getInstance(),"debug:reconnectMqtt服务器连接成功");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        connection.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
                        callback.MqttConnectFail();
                        ToastUtils.showShort(App.getInstance(), "debug:reconnectMqtt服务器连接失败");
                    }
                });
            } catch (MqttException e1) {
                e1.printStackTrace();
            }
        }else{
            //无网络
//            Message msg = Message.obtain();
//            msg.what = 0;
//            handler.sendMessageDelayed(handler.obtainMessage(0), 5000);
        }
    }



    public void getMqttConnection(){
        try {
            MyLog.d("getMqttConnection", "1");
            mac.connect(connection.getConnectionOptions(), this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    onMqttConnectListener.MqttConnectSuccess();
                    MyLog.d("getMqttConnection", "MqttConnectSuccess 连接服务器成功");
                    ServiceConstants.connection_status = "connected";
                    connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTED);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    onMqttConnectListener.MqttConnectFail();
                    MyLog.d("getMqttConnection", "MqttConnectSuccess 连接服务器失败");
                    connection.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
                }
            });
//            MyLog.d("getMqttConnection", "2");
//            Connections.getInstance(mcontext).addConnection(connection);
//            MyLog.d("getMqttConnection", "3");
        } catch (MqttException e1) {
            e1.printStackTrace();
        }
    }

//    public void removeConnectionInDatabase(){
//        Connections.getInstance(mcontext).removeConnection(connection);
//    }

    public void MqttDisconnect(){
        if(returnMqttStatus()){
            try{
                mac.disconnect(this,new IMqttActionListener(){
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        MyLog.d("MqttDisconnect","断开连接成功");
                        mac = null;
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        MyLog.d("MqttDisconnect", "断开连接失败");
                    }

                });
                MyLog.d("MqttDisconnect","MqttDisconnect");
            }catch(MqttException e){
                e.printStackTrace();
            }
        }
    }


    public interface OnMqttConnectListener {
        void MqttConnectSuccess();
        void MqttConnectFail();
    }

    public void setOnMqttConnectListener(OnMqttConnectListener var1) {
        this.onMqttConnectListener = var1;
    }

    public Boolean returnMqttStatus(){
        if(mac != null&& mac.isConnected()){
            return true;
        }
        else{
            return false;
        }
    }

    public MqttAndroidClient getMac(){
        return mac;
    }

    public static void sendMessage(final byte[] message, final String IMEI,Callback callback) {
        if(NetworkUtils.isNetworkConnected(App.getInstance())){
            Connection c = Connections.getInstance(App.getInstance()).getConnection(ServiceConstants.handler);
            if (c.getClient() == null||!c.getClient().isConnected()) {
                callback.onFail(new Exception("请先连接设备"));
//                ToastUtils.showShort(App.getInstance(), "请先连接设");
                return;
            }
            try {
                //向服务器发送命令
                c.getClient().publish("app2dev/" + IMEI + "/cmd", message,
                        ServiceConstants.MQTT_QUALITY_OF_SERVICE, false, null,
                        new ActionListener(IMEI, App.getInstance(), ActionListener.Action.PUBLISH,
                                ServiceConstants.handler, callback));
            } catch (MqttException e) {
                e.printStackTrace();
                callback.onFail(e);
            }
        }else{
            callback.onFail(new Exception("无网络连接"));
        }
    }

    public void sendMessage(final byte[] message, final String IMEI) {
        if(NetworkUtils.isNetworkConnected(App.getInstance())){
            Connection c = Connections.getInstance(App.getInstance()).getConnection(ServiceConstants.handler);
            if (c.getClient() == null||!c.getClient().isConnected()) {
                ToastUtils.showShort(App.getInstance(), "请先连接设备，或等待连接。");
                return;
            }
            try {
                //向服务器发送命令
                mac.publish("app2dev/" + IMEI + "/cmd", message,
                        ServiceConstants.MQTT_QUALITY_OF_SERVICE, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else{
//            callback.onFail();
        }
    }

    public void subscribe(String IMEI){
        //订阅命令字
        String topic1 = "dev2app/" + IMEI + "/cmd";
        //订阅GPS数据
//        String topic2 = "dev2app/" + IMEI + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + IMEI + "/433";
        //订阅报警
        String topic4 = "dev2app/" + IMEI + "/alarm";

        String topic5 = "dev2app/" + IMEI + "/notify";

        String[] topic = {topic1, topic3, topic4, topic5};
        int[] qos = {ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE};
        try {
            mac.subscribe(topic, qos);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic1);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic3);
            LogUtil.log.i("Connection established to " + ServiceConstants.MQTT_HOST + " on topic " + topic4);
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(App.getInstance(), "订阅失败!请稍后重启再试！");
        }
    }


    public void subscribe(String IMEI,Callback callback){
        //订阅命令字
        String topic1 = "dev2app/" + IMEI + "/cmd";
        //订阅GPS数据
        String topic2 = "dev2app/" + IMEI + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + IMEI + "/433";
        //订阅报警
        String topic4 = "dev2app/" + IMEI + "/alarm";

        String topic5 = "dev2app/" + IMEI + "/notify";

        String[] topic = {topic1, topic2,topic3, topic4, topic5};
        int[] qos = {ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE,
                ServiceConstants.MQTT_QUALITY_OF_SERVICE};

        Connection c = Connections.getInstance(App.getInstance()).getConnection(ServiceConstants.handler);
        if(c.getClient()!=null&&c.getClient().isConnected()){
            try {
                Connections.getInstance(App.getInstance()).getConnection(ServiceConstants.handler).getClient()
                        .subscribe(topic, qos, null,
                                new ActionListener(IMEI, App.getInstance(),
                                        ActionListener.Action.SUBSCRIBE,
                                        ServiceConstants.handler,callback));
            } catch (MqttException e) {
                e.printStackTrace();
                LogUtil.log.i(IMEI + EventbusConstants.SUB_FAIL);
            }
        }else{
            callback.onFail(new Exception("请先连接设备"));
        }
    }

    public boolean unSubscribe(String IMEI) {
        //订阅命令字
        String topic1 = "dev2app/" + IMEI + "/cmd";
        String topic2 = "dev2app/" + IMEI + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + IMEI + "/433";

        String topic4 = "dev2app/" + IMEI + "/alarm";

        String topic5 = "dev2app/" + IMEI + "/notify";
        String[] topic = {topic1, topic2,topic3, topic4, topic5};
        try {
            mac.unsubscribe(topic);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(App.getInstance(), "取消订阅失败!请稍后重启再试！");
            return false;
        }
    }


    public void unSubscribe(String IMEI,Callback callback) {
        //订阅命令字
        String topic1 = "dev2app/" + IMEI + "/cmd";
        String topic2 = "dev2app/" + IMEI + "/gps";
        //订阅上报的信号强度
        String topic3 = "dev2app/" + IMEI + "/433";

        String topic4 = "dev2app/" + IMEI + "/alarm";

        String topic5 = "dev2app/" + IMEI + "/notify";
        String[] topic = {topic1, topic2,topic3, topic4, topic5};

        Connection c = Connections.getInstance(App.getInstance()).getConnection(ServiceConstants.handler);
        if(c.getClient()!=null&&c.getClient().isConnected()){
            try {
                mac.unsubscribe(topic,null,new ActionListener(IMEI, App.getInstance(),
                        ActionListener.Action.UNSUBSCRIBE, ServiceConstants.handler, callback));
            } catch (MqttException e) {
                e.printStackTrace();
                ToastUtils.showShort(App.getInstance(), "取消订阅失败!请稍后重启再试！");
//                EventBus.getDefault().post(
//                        new FirstEvent(IMEI + EventbusConstants.UNSUB_FAIL));
                LogUtil.log.i(IMEI + EventbusConstants.UNSUB_FAIL);
                callback.onFail(new Exception("unSubscribe出错"));
            }
        }else{
            callback.onFail(new Exception("无网络连接"));
        }

    }

    public boolean unSubscribeGPS(String IMEI) {
        String topic = "dev2app/" + IMEI + "/gps";
        try {
            if(returnMqttStatus()){
                mac.unsubscribe(topic);
                mac.unsubscribe(topic);
                return true;
            }
            return false;
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(App.getInstance(), "取消订阅失败!请稍后重启再试！");
            return false;
        }
    }


    public void subscribeGPS(String IMEI){
        String topic = "dev2app/" + IMEI + "/gps";
        try {
            if(returnMqttStatus()){
                mac.subscribe(topic, ServiceConstants.MQTT_QUALITY_OF_SERVICE);
            }
        } catch (MqttException e) {
            e.printStackTrace();
            ToastUtils.showShort(App.getInstance(), "订阅失败!请稍后重启再试！");
        }
    }

    public interface Callback{
        public void onSuccess();
        public void onFail(Exception e);

    }
}
