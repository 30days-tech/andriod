package com.thirtydays.common.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.app.ActivityManager;
import android.app.Application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxiaojin on 2017/10/19.
 * app工具类: 获取app版本、判断是否在前台, 判断app是否安装等功能
 */

public class AppUtil {
    private static final String TAG = "AppUtil";

    /**
     * 检测app是否已安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    /**
     * 获取当前进程名
     *
     * @return 进程名
     */
    public static String getProcessName() {
        long curTime = System.currentTimeMillis();
        BufferedReader reader = null;
        try {
            int pid = android.os.Process.myPid();
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            Log.e(TAG, "Get current process name: " + processName + ", cost time:" + (System.currentTimeMillis() - curTime));
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Log.e(TAG, "Get current process name failed, exception:" + throwable.getMessage(), throwable);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取app版本名称
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String pkName = context.getPackageName();
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(pkName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Get versionName or versionCode failed.", e);
            return "";
        }
        return versionName;
    }

    /**
     * 获取app versioncode
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        String pkName = context.getPackageName();
        int versionCode;
        try {
            versionCode = context.getPackageManager().getPackageInfo(pkName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Get versionName or versionCode failed.", e);
            return 0;
        }
        return versionCode;
    }

    /**
     * 判断APP是否在前台
     *
     * @param application
     * @return
     */
    public static boolean isAppOnForeground(Application application) {
        ActivityManager activityManager = (ActivityManager) application.getApplicationContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        String packageName = application.getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;

            }
        }
        return false;
    }
}
