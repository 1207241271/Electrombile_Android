package com.xunce.electrombile.activity.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.xunce.electrombile.R;
import com.xunce.electrombile.utils.system.ToastUtils;
import com.xunce.electrombile.utils.useful.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgetPassActivity2 extends Activity {

    private EditText et_PhoneNumber;
    private Button btn_NextStep;
    private String phone;
    ProgressDialog dialog;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {
                case TOAST:
                    dialog.cancel();
                    ToastUtils.showShort(ForgetPassActivity2.this, (String) msg.obj);
                    if(msg.obj.toString().equals("发送成功")){
                        //跳转到下一个页面
                        Intent intent = new Intent(ForgetPassActivity2.this,ResetPassActivity.class);
                        intent.putExtra("phone",et_PhoneNumber.getText().toString());
                        startActivity(intent);
                        Log.d("test", "test");
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass2);
        initView();
        initEvent();
    }

    private void initView(){
        et_PhoneNumber = (EditText)findViewById(R.id.et_PhoneNumber);
        btn_NextStep = (Button)findViewById(R.id.btn_NextStep);
        dialog = new ProgressDialog(ForgetPassActivity2.this);
        dialog.setMessage("处理中，请稍候...");
    }

    private void initEvent(){
        btn_NextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = et_PhoneNumber.getText().toString().trim();

                if(!IsPhoneNumberOK(phone,ForgetPassActivity2.this)){
                    return;
                }
                else{
                    sendVerifyCode(phone);
                }

            }
        });
    }

    //用一个静态方法来判断手机号是否合理
    public static Boolean IsPhoneNumberOK(String phone, Context context){
        if (StringUtils.isEmpty(phone)) {
            ToastUtils.showShort(context, "手机号码不能为空");
            return false;
        }

        /**
         * 手机号码:
         * 13[0-9], 14[5,7], 15[0, 1, 2, 3, 5, 6, 7, 8, 9], 17[0, 1, 6, 7, 8], 18[0-9]
         * 移动号段: 134,135,136,137,138,139,147,150,151,152,157,158,159,170,178,182,183,184,187,188
         * 联通号段: 130,131,132,145,152,155,156,170,171,176,185,186
         * 电信号段: 133,134,153,170,177,180,181,189
         */
//        Pattern p = Pattern.compile("^1(3[0-9]|4[57]|5[0-35-9]|7[01678]|8[0-9])\\\\d{8}$");
        //我们不需要那么精准，用下面的模式检查就可以了
        Pattern p = Pattern.compile("^1[3|4|5|7|8][0-9]\\d{8}]$");
        Matcher m = p.matcher(phone);
        if (!m.matches()) {
            ToastUtils.showShort(context, "手机号码不正确");
            return false;
        }
        return true;
    }

    private void sendVerifyCode(final String phone) {
        dialog.show();
        //发送请求验证码指令
        AVUser.requestPasswordResetBySmsCodeInBackground(phone, new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = "发送成功";
                    handler.sendMessage(msg);
                } else {
                    LogUtil.log.i(e.toString());
                    Message msg = new Message();
                    msg.what = handler_key.TOAST.ordinal();
                    msg.obj = e.toString();
                    if(e.getCode() == 213){
                        //不存在该用户
                        msg.obj = "不存在该用户";
                    }
                    handler.sendMessage(msg);
                }
            }
        });
    }

    enum handler_key{
        TOAST,
    };

}
