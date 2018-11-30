package com.hunter.wallet.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;

import com.hunter.wallet.activity.WalletInfoActivity;

import java.util.List;

public class AppUtils {

    // 应用是否在前台
    public static boolean isFront(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0 && list.get(0).topActivity.getPackageName().equals(context.getPackageName()) && powerManager.isScreenOn()) {
            return true;
        } else {
            return false;
        }
    }

    // 重启应用
    public static void restartApp(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = activity.getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(i);
                activity.finish();
            }
        });
    }

    public static void startActivity(Activity activity, Class<?> cls, int flags) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(activity, cls);
                intent.setFlags(flags);
                activity.startActivity(intent);
            }
        });
    }
}
