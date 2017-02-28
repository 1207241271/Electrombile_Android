package com.xunce.electrombile.utils.useful;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by yangxu on 2017/1/17.
 */

public class PermissionChecker {
    private final Context mContext;
    private static final int PERMISSION_REQUEST_CODE = 0;


    public PermissionChecker(Context context) {
        mContext = context.getApplicationContext();
    }

    // 判断是否缺少权限
    public boolean lakesPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lakesPermission(permission))
                return true;
        }
        return false;
    }

    private boolean lakesPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED;
    }

}
