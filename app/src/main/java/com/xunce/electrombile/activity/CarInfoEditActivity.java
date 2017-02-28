package com.xunce.electrombile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.*;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogUtil;
import com.xunce.electrombile.Callback;
import com.xunce.electrombile.LeancloudManager;
import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.account.LoginActivity;
import com.xunce.electrombile.applicatoin.App;
import com.xunce.electrombile.manager.CmdCenter;
import com.xunce.electrombile.manager.SettingManager;
import com.xunce.electrombile.services.HttpService;
import com.xunce.electrombile.utils.system.BitmapUtils;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.JPushUtils;
import com.xunce.electrombile.utils.useful.NetworkUtils;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CarInfoEditActivity extends Activity implements View.OnClickListener,ServiceConnection {
    private PopupWindow mpopupWindow;
//    private TextView tv_CarIMEI;
//    private RelativeLayout btn_DeleteDevice;
    private String IMEI;
    private SettingManager setManager;
    private ProgressDialog progressDialog;
    private RelativeLayout btn_DeviceChange;
    private Boolean Flag_Maincar;
//    private Boolean LastCar;
    private MqttConnectManager mqttConnectManager;
    public CmdCenter mCenter;
    private String NextCarIMEI;
    private List<String> IMEIlist;
    private int othercarListPosition;
    private TextView tv_CarName;
    private LeancloudManager leancloudManager;
    private TextView titleTextView;
    private ImageView img_car;
    private Uri imageUri;
    private Bitmap bitmap;
    private JPushUtils jPushUtils;

    public static final int TAKE_PHOTE=1;
    public static final int CROP_PHOTO=2;
    public static final int CHOOSE_PHOTO=3;

    private HttpService.Binder httpBinder = null;
    private HttpService         httpService;


    private void deleteCarInfo(){
//        String fileName = Environment.getExternalStorageDirectory() + "/"+IMEI+"crop_result.jpg";
//        File f = new File(fileName);
        File f = new File(this.getExternalFilesDir(null), setManager.getIMEI()+"crop_result.jpg");
        if (f.exists()){
            f.delete();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_pop_FromGallery:
                File outputImage = new File(Environment.getExternalStorageDirectory(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch(IOException e){
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.intent.action.PICK");
                intent.setType("image/*");
                startActivityForResult(intent,CHOOSE_PHOTO);
                mpopupWindow.dismiss();
                break;

            case R.id.tv_pop_camera:
                File outputImage1 = new File(Environment.getExternalStorageDirectory(),"output_image.jpg");
                try{
                    if(outputImage1.exists()){
                        outputImage1.delete();
                    }
                    outputImage1.createNewFile();
                }catch(IOException e){
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage1);
                Intent intent1 = new Intent("android.media.action.IMAGE_CAPTURE");
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent1,TAKE_PHOTE);
                mpopupWindow.dismiss();
                break;

            case R.id.tv_pop_cancel:
                mpopupWindow.dismiss();
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case TAKE_PHOTE:
                if(resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(this,CropActivity.class);
                    intent.setData(imageUri);
                    intent.putExtra("IMEI",IMEI);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;

            case CHOOSE_PHOTO:
                if(resultCode == Activity.RESULT_OK) {
                    imageUri = data.getData();
                    Intent intent = new Intent(this,CropActivity.class);
                    intent.setData(imageUri);
                    intent.putExtra("IMEI", IMEI);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;

            case CROP_PHOTO:
                if(resultCode == Activity.RESULT_OK) {
                    img_car.setImageBitmap(null);
                    bitmapRelease();
                    bitmap = BitmapUtils.compressImageFromFile(IMEI);
                    if (bitmap != null) {
                        img_car.setImageBitmap(bitmap);
                        //发送广播 switchfragment的照片啊
                        Intent intent = new Intent("com.app.bc.test");
                        intent.putExtra("KIND", "CHANGEMAINPIC");
                        sendBroadcast(intent);//发送广播事件
                    }
                }
                break;
            default:
                break;
        }
    }

    public void bitmapRelease(){
        if(bitmap != null && !bitmap.isRecycled()){
            // 回收并且置为null
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info_edit);

        Intent intent = getIntent();
        IMEI = intent.getStringExtra("string_key");
        NextCarIMEI = intent.getStringExtra("NextCarIMEI");
        othercarListPosition = intent.getIntExtra("list_position",0);

        initView();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    void initView(){
        View titleView = findViewById(R.id.ll_button) ;
        titleTextView = (TextView)titleView.findViewById(R.id.tv_title);
        RelativeLayout btn_back = (RelativeLayout)titleView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CarInfoEditActivity.this.finish();
            }
        });

        setManager = SettingManager.getInstance();
        leancloudManager = LeancloudManager.getInstance();
        jPushUtils = JPushUtils.getInstance();

        img_car = (ImageView)findViewById(R.id.img_car);
        Bitmap bitmap = BitmapUtils.compressImageFromFile(IMEI);
        if(bitmap!=null){
            img_car.setImageBitmap(bitmap);
        }

        TextView tv_CarIMEI = (TextView)findViewById(R.id.tv_CarIMEI);
        tv_CarIMEI.setText("设备号:"+IMEI);

        tv_CarName = (TextView)findViewById(R.id.tv_CarName);
        tv_CarName.setText("车辆名称:"+setManager.getCarName(IMEI));

        TextView tv_createTime = (TextView)findViewById(R.id.tv_createTime);
        tv_createTime.setText("绑定日期:"+setManager.getCreateTime(IMEI));

        TextView tv_phoneNumber = (TextView)findViewById(R.id.tv_phoneNumber);
        String s = "手机号:"+setManager.getPhoneNumber();
        tv_phoneNumber.setText(s);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在设置,请稍后");

        RelativeLayout btn_DeleteDevice = (RelativeLayout)findViewById(R.id.relativelayout_DeviceUnbind);
        btn_DeleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkUtils.checkNetwork(CarInfoEditActivity.this)){
                    ToastUtils.showShort(CarInfoEditActivity.this, "请检查网络连接,该操作无法完成");
                    return;
                }
                progressDialog.show();
                releaseBinding();
            }
        });

        //设备切换
        btn_DeviceChange = (RelativeLayout)findViewById(R.id.relativeLayout_DeviceChange);
        btn_DeviceChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkUtils.checkNetwork(CarInfoEditActivity.this)){
                    ToastUtils.showShort(CarInfoEditActivity.this, "请检查网络连接,切换无法完成");
                    return;
                }
                DeviceChange();
            }
        });

        //修改车的昵称
        RelativeLayout RelativeLayout_changeCarName = (RelativeLayout)findViewById(R.id.RelativeLayout_changeCarName);
        RelativeLayout_changeCarName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCarNickName();
            }
        });

        RelativeLayout RelativeLayout_changeCarPic = (RelativeLayout)findViewById(R.id.RelativeLayout_changeCarPic);
        RelativeLayout_changeCarPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopMenu();
            }
        });

        JudgeMainCarOrNot();
        IMEIlist = setManager.getIMEIlist();

        Intent intent = new Intent(CarInfoEditActivity.this, HttpService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }
    
    private void changeCarNickName(){
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_changenickname, null);
        final Dialog dialog = new Dialog(CarInfoEditActivity.this, R.style.Translucent_NoTitle_white);

        Button btn_suretochangeName = (Button)view.findViewById(R.id.btn_sure);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        final EditText et_nickname = (EditText)view.findViewById(R.id.et_nickname);
        TextView tv_title = (TextView)view.findViewById(R.id.title);
        tv_title.setText("修改车辆名称");

        btn_suretochangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = et_nickname.getText().toString();
                if(nickname.equals("")){
                    dialog.dismiss();
                }
                else{
                    tv_CarName.setText("车辆名称:"+nickname);
                    dialog.dismiss();
                    //上传服务器
                    leancloudManager.uploadCarName(IMEI, nickname);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        WindowManager m = CarInfoEditActivity.this.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        final int dialog_width = (int) (d.getWidth() * 0.75); // 宽度设置为屏幕的0.65

        dialog.addContentView(view, new LinearLayout.LayoutParams(
                dialog_width, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.show();
    }

    //设备解绑
//    private void DeviceUnbinded(){
//      //解绑的不是最后一辆车
//        if(Flag_Maincar){
//            if(mqttConnectManager.returnMqttStatus()){
//                mqttConnectManager.unSubscribe(IMEI);
//                if(!NextCarIMEI.equals("空")){
//                    setManager.setIMEI(NextCarIMEI);
//                    mqttConnectManager.subscribe(NextCarIMEI);
//                    mqttConnectManager.sendMessage(mCenter.getInitialStatus(), NextCarIMEI);
//
//                    IMEIlist.remove(0);
//                    setManager.setIMEIlist(IMEIlist);
//
//                    jPushUtils.setJPushAlias("simcom_" + setManager.getIMEI());
//                }
//                else{
//                    Log.d("test","test");
//                }
//            }
//            else{
//                ToastUtils.showShort(CarInfoEditActivity.this, "mqtt连接断开");
//            }
//        }
//
//        else{
//            IMEIlist.remove(othercarListPosition+1);
//            setManager.setIMEIlist(IMEIlist);
//        }
//
//        //删除头像文件
//        deleteCarInfo();
//        setManager.removeKey(IMEI);
//
//        Intent intent = new Intent();
//        intent.putExtra("string_key","设备解绑");
//        intent.putExtra("boolean_key", Flag_Maincar);
//        setResult(RESULT_OK, intent);
//        finish();
//    }

    //设备切换
    private void DeviceChange(){
        //在这里就解订阅原来的设备号,并且订阅新的设备号,然后查询小安宝的初始状态
        mqttConnectManager = MqttConnectManager.getInstance();
        if(mqttConnectManager.returnMqttStatus()){
            progressDialog.show();

            jPushUtils.setJPushAlias("simcom_" + IMEI, new Callback() {
                @Override
                public void onSuccess() {
                    mqttConnectManager.unSubscribe(setManager.getIMEI(), new MqttConnectManager.Callback() {
                        @Override
                        public void onSuccess() {
                            mqttConnectManager.subscribe(IMEI, new MqttConnectManager.Callback() {
                                @Override
                                public void onSuccess() {
                                    LogUtil.log.i("subscribe-onSuccess");
                                    setManager.setIMEI(IMEI);
                                    mCenter = CmdCenter.getInstance();
                                    mqttConnectManager.sendMessage(mCenter.getInitialStatus(), IMEI);
                                    //更新IMEIlist
                                    String IMEI_previous = IMEIlist.get(0);
                                    IMEIlist.set(0, IMEI);
                                    IMEIlist.set(othercarListPosition + 1, IMEI_previous);
                                    setManager.setIMEIlist(IMEIlist);
                                    ToastUtils.showShort(CarInfoEditActivity.this, "切换设备成功");
                                    progressDialog.dismiss();
                                    setManager.setPhoneIsAgree(false);

                                    Intent intent = new Intent();
                                    intent.putExtra("string_key", "设备切换");
                                    intent.putExtra("boolean_key", Flag_Maincar);
                                    setResult(RESULT_OK, intent);
                                    CarInfoEditActivity.this.finish();
                                }

                                @Override
                                public void onFail(Exception e) {
                                    progressDialog.dismiss();
//                                  Subscribe失败
                                    ToastUtils.showShort(App.getInstance(),
                                            "切换设备失败,"+e.getMessage()+",请退出登录");
                                }
                            });
                        }

                        @Override
                        public void onFail(Exception e) {
                            progressDialog.dismiss();
//                            unSubscribe失败
                            ToastUtils.showShort(App.getInstance(),
                                    "切换设备失败,"+e.getMessage()+",请退出登录");
                        }
                    });
                }

                @Override
                public void onFail() {
                    progressDialog.dismiss();
//                    setJPushAlias失败
                    ToastUtils.showShort(App.getInstance(), "切换设备失败,请稍后再试");
                }
            });
        }
        else{
            ToastUtils.showShort(CarInfoEditActivity.this,"mqtt连接失败");
        }
    }



    //在Binding数据表里删除一条记录
    private void DeleteInBindingList(final Callback callback){
        AVQuery<AVObject> query = new AVQuery<>("Bindings");
        //通过下面这两个约束条件,唯一确定了一条记录
        query.whereEqualTo("IMEI", IMEI);
        query.whereEqualTo("user", AVUser.getCurrentUser());

        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    //list的size一定是1
                    if (list.size() > 0) {
                        AVObject avObject = list.get(0);
                        avObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    //成功删除了记录
//                                    Message msg = Message.obtain();
//                                    msg.what = handler_key.DELETE_SUCCESS.ordinal();
//                                    mHandler.sendMessage(msg);
                                    callback.onSuccess();

                                } else {
                                    callback.onFail();
                                }
                            }
                        });
                    }
                    if (list.size() > 1) {
                        LogUtil.log.i("list的size一定是1  哪里出错了?");
                    }
                } else {
                    callback.onFail();
                }
            }
        });
    }

    //设备解绑
    private void releaseBinding() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            ToastUtils.showShort(this, "无网络连接");
            progressDialog.dismiss();
            return;
        }
        mqttConnectManager = MqttConnectManager.getInstance();
        mCenter = CmdCenter.getInstance();
        //删除本地的数据库文件  待测试
//        deleteDatabaseFile();
        ReleaseThreeSituation();
//        QueryBindList();
    }

    private void ReleaseSituation(int i){
        switch (i){
            case 1:
                //1.只剩下一个设备,解绑该设备:
                if (mqttConnectManager.returnMqttStatus()) {
                    jPushUtils.setJPushAlias("simcom", new Callback() {
                        @Override
                        public void onSuccess() {
                            mqttConnectManager.unSubscribe(IMEI, new MqttConnectManager.Callback() {
                                @Override
                                public void onSuccess() {
                                    DeleteInBindingList(new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            progressDialog.dismiss();
                                            //关闭mqttclient
                                            mqttConnectManager.MqttDisconnect();

                                            deleteDatabaseFile();

                                            //关闭小安宝报警的服务
                                            Intent intent;
                                            intent = new Intent();
                                            intent.setAction("com.xunce.electrombile.alarmservice");
                                            intent.setPackage(CarInfoEditActivity.this.getPackageName());
                                            CarInfoEditActivity.this.stopService(intent);

                                            AVUser currentUser = AVUser.getCurrentUser();
                                            currentUser.logOut();

                                            //IMEIlist更新
                                            if(IMEIlist.size()>0){
                                                IMEIlist.remove(0);
                                                setManager.setIMEIlist(IMEIlist);
                                            }
                                            setManager.setFirstLogin(true);

                                            //删除设备头像    sharepreference中的部分信息:IMEI号码对应的绑定日期和车昵称
                                            deleteCarInfo();
                                            setManager.removeKey(IMEI);
                                            FragmentActivity.cancelAllNotification();
                                            intent = new Intent(CarInfoEditActivity.this, LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onFail() {
                                            progressDialog.dismiss();
                                            LogUtil.log.i("ReleaseSituation--unSubscribe失败");
                                            ToastUtils.showShort(CarInfoEditActivity.this, "解绑失败,请退出登录重试");
                                        }
                                    });
                                }

                                @Override
                                public void onFail(Exception e) {
                                    progressDialog.dismiss();
                                    LogUtil.log.i("ReleaseSituation--unSubscribe失败");
                                    ToastUtils.showShort(CarInfoEditActivity.this, "解绑失败,请退出登录重试");
                                }
                            });
                        }

                        @Override
                        public void onFail() {
                            progressDialog.dismiss();
                            LogUtil.log.i("ReleaseSituation--setJPushAlias失败");
                            ToastUtils.showShort(CarInfoEditActivity.this, "解绑失败,请稍后重试");
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    ToastUtils.showShort(CarInfoEditActivity.this, "请先连接设备");
                }
                break;

            case 2:
//                2.剩下多个设备,解绑主设备
                if(mqttConnectManager.returnMqttStatus()){
                    jPushUtils.setJPushAlias("simcom_" + NextCarIMEI, new Callback() {
                        @Override
                        public void onSuccess() {
                            mqttConnectManager.unSubscribe(IMEI, new MqttConnectManager.Callback() {
                                @Override
                                public void onSuccess() {
                                    mqttConnectManager.subscribe(NextCarIMEI, new MqttConnectManager.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            DeleteInBindingList(new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    progressDialog.dismiss();
                                                    setManager.setIMEI(NextCarIMEI);
//                                                    mqttConnectManager.subscribe(NextCarIMEI);
                                                    mqttConnectManager.sendMessage(mCenter.getInitialStatus(), NextCarIMEI);

                                                    IMEIlist.remove(0);
                                                    setManager.setIMEIlist(IMEIlist);
                                                    deleteAndToActivity();
                                                }

                                                @Override
                                                public void onFail() {
                                                    progressDialog.dismiss();
                                                    LogUtil.log.i("leancloud删除失败");
                                                    ToastUtils.showShort(CarInfoEditActivity.this, "解绑失败,请退出登录重试");
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFail(Exception e) {
                                            progressDialog.dismiss();
                                            LogUtil.log.i("ReleaseSituation--Subscribe失败");
                                            ToastUtils.showShort(CarInfoEditActivity.this, "解绑失败,请退出登录重试");
                                        }
                                    });
                                }

                                @Override
                                public void onFail(Exception e) {
                                    progressDialog.dismiss();
                                    LogUtil.log.i("ReleaseSituation--unSubscribe失败");
                                    ToastUtils.showShort(CarInfoEditActivity.this, "解绑失败,请退出登录重试");
                                }
                            });

                        }

                        @Override
                        public void onFail() {
                            progressDialog.dismiss();
                            LogUtil.log.i("ReleaseSituation--setJPushAlias失败");
                            ToastUtils.showShort(CarInfoEditActivity.this, "解绑失败,请稍后重试");
                        }
                    });

                }else{
                    progressDialog.dismiss();
                    ToastUtils.showShort(CarInfoEditActivity.this, "请先连接设备");
                }
                break;
            case 3:
//                3.剩下多个设备,解绑从设备
                DeleteInBindingList(new Callback() {
                    @Override
                    public void onSuccess() {
                        progressDialog.dismiss();
                        IMEIlist.remove(othercarListPosition+1);
                        setManager.setIMEIlist(IMEIlist);
                        deleteAndToActivity();
                    }

                    @Override
                    public void onFail() {
                        progressDialog.dismiss();
                        LogUtil.log.i("leancloud删除设备失败");
                        ToastUtils.showShort(CarInfoEditActivity.this, "解绑失败,请稍后重试");
                    }
                });
                break;
        }

    }

    private void deleteAndToActivity(){
        deleteDatabaseFile();
        //删除头像文件
        deleteCarInfo();
        //取消电话报警

        String url = SettingManager.getInstance().getHttpHost()+SettingManager.getInstance().getHttpPort()+"/v1/telephone/" + IMEI;
        if(httpService != null && setManager.getPhoneIsAlarm()){
            try {
                try {
                    JSONObject telephone = new JSONObject();
                    telephone.put("telephone",AVUser.getCurrentUser().getUsername());
                    httpService.dealWithHttpResponse(url,2,"phoneAlarmTest",telephone.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        setManager.removeKey(IMEI);

        Intent intent = new Intent();
        intent.putExtra("string_key", "设备解绑");
        intent.putExtra("boolean_key", Flag_Maincar);
        setResult(RESULT_OK, intent);
        finish();
    }


    private void ReleaseThreeSituation(){
        //解绑的时候有三种情况:1.只剩下一个设备,解绑该设备;2.剩下多个设备,解绑主设备;3.剩下多个设备,解绑从设备
        List<String> IMEIlist = setManager.getIMEIlist();
        if(IMEIlist.size() == 1){
            AlertDialog.Builder builder = new AlertDialog.Builder(CarInfoEditActivity.this);
            builder.setMessage("因没有绑定设备,解绑该设备后将弹出登录界面");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    LastCar = true;
                    dialog.dismiss();
                    //1.只剩下一个设备,解绑该设备
                    ReleaseSituation(1);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    progressDialog.dismiss();
                }
            });
            Dialog dialog = builder.create();
            dialog.show();
        }
        else if(IMEIlist.size() == 0){
            ToastUtils.showShort(CarInfoEditActivity.this,"无任何绑定设备,请先绑定设备");
        }
        else{
            if(setManager.getIMEI().equals(IMEI)){
                Flag_Maincar = true;
//                2.剩下多个设备,解绑主设备
                ReleaseSituation(2);
            }
            else{
//                2.剩下多个设备,解绑从设备
                Flag_Maincar = false;
                ReleaseSituation(3);
            }
        }
    }

    //解绑一台设备的时候需要把本地相关的一级和二级数据库文件也删除掉
    private void deleteDatabaseFile(){
        String packageName = getPackageName();
        String path = "/data/data/"+packageName+"/databases";
        File file = new File(path);
        if(file.exists()){
            File[] files = file.listFiles();
            for(File file1:files){
                String fileName = file1.getName();
                if(fileName.contains(IMEI)){
                    file1.delete();
                }
            }
        }
    }

    enum handler_key{
        DELETE_RECORD,
        DELETE_SUCCESS,
    }

//    public void QueryBindList(){
//        List<String> IMEIlist = setManager.getIMEIlist();
//
//        if(IMEIlist.size() == 1){
//            AlertDialog.Builder builder = new AlertDialog.Builder(CarInfoEditActivity.this);
//            builder.setMessage("因没有绑定设备,解绑该设备后将弹出登录界面");
//            builder.setTitle("提示");
//            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    LastCar = true;
//                    dialog.dismiss();
//
//                    android.os.Message msg = Message.obtain();
//                    msg.what = handler_key.DELETE_RECORD.ordinal();
//                    mHandler.sendMessage(msg);
//
//                }
//            });
//            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    progressDialog.dismiss();
//                }
//            });
//            Dialog dialog = builder.create();
//            dialog.show();
//        }
//
//        else if(IMEIlist.size() == 0){
//           ToastUtils.showShort(CarInfoEditActivity.this,"无任何绑定设备,请先绑定设备");
//        }
//
//        else{
//            if(setManager.getIMEI().equals(IMEI)){
//                //1.解绑主设备
//                Flag_Maincar = true;
//            }
//            else{
//                //2.解绑从设备
//                Flag_Maincar = false;
//            }
//            android.os.Message msg = Message.obtain();
//            msg.what = handler_key.DELETE_RECORD.ordinal();
//            mHandler.sendMessage(msg);
//        }
//    }

    //判断正在查看的设备是否是主设备
    void JudgeMainCarOrNot(){
        if(setManager.getIMEI().equals(IMEI)){
            Flag_Maincar = true;
            titleTextView.setText("主车辆");
            btn_DeviceChange.setVisibility(View.INVISIBLE);
        }
        else{
            Flag_Maincar = false;
            titleTextView.setText("其他车辆");
        }
    }

    private void showPopMenu() {
        View view = View.inflate(this, R.layout.popwindow_changecarpic, null);
        TextView tv_pop_FromGallery = (TextView) view.findViewById(R.id.tv_pop_FromGallery);
        TextView tv_pop_camera = (TextView) view.findViewById(R.id.tv_pop_camera);
        TextView tv_pop_cancel = (TextView) view.findViewById(R.id.tv_pop_cancel);
        tv_pop_FromGallery.setOnClickListener(this);
        tv_pop_camera.setOnClickListener(this);
        tv_pop_cancel.setOnClickListener(this);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mpopupWindow.dismiss();
            }
        });

        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        LinearLayout ll_popup_carpic = (LinearLayout) view.findViewById(R.id.ll_popup_carpic);
        ll_popup_carpic.startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_bottom_in));

        if(mpopupWindow==null){
            mpopupWindow = new PopupWindow(this);
            mpopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            mpopupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            mpopupWindow.setBackgroundDrawable(new BitmapDrawable());

            mpopupWindow.setFocusable(true);
            mpopupWindow.setOutsideTouchable(true);

            //背景设置为半透明
            ColorDrawable dw = new ColorDrawable(0xb0000000);
            mpopupWindow.setBackgroundDrawable(dw);
        }

        mpopupWindow.setContentView(view);
        mpopupWindow.showAtLocation(img_car, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        mpopupWindow.update();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder){
        httpBinder = (HttpService.Binder) iBinder;
        httpBinder.getHttpService().setCallback(new HttpService.Callback(){
            @Override
            public void onGetResponse(String data,String type){
            }

            @Override
            public void dealError(short errorCode) {
            }

        });
        httpService = httpBinder.getHttpService();
        //连接完成后查询数据
    }
}
