package com.xunce.electrombile.utils.device;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.xunce.electrombile.R;
import com.xunce.electrombile.activity.FragmentActivity;

//import io.yunba.android.manager.YunBaManager;

/**
 * Created by lybvinci on 2015/5/13.
 */
public class DeviceUtils {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean showNotifation(Context context, String topic, String msg) {
        try {
            Uri alarmSound = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            long[] pattern = { 500, 500, 500 };
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context).setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(topic).setContentText(msg)
                    .setSound(alarmSound).setVibrate(pattern).setAutoCancel(true);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, FragmentActivity.class);

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void wakeUpAndUnlock(Context context){
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        kl.disableKeyguard();
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        wl.acquire();
        wl.release();
    }
}
