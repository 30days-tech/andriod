package com.thirtydays.pushservice.util;

import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.thirtydays.pushservice.constant.PushConstant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by chenxiaojin on 2017/8/11.
 */

public class SystemUtil {

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    /**
     * 获取进程号对应的进程名
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
            return processName;
        } catch (Throwable throwable) {
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
     * 获取手机品牌
     *
     * @return
     */
    public static String getPhoneBrand() {
        String SYS = PushConstant.BRAND_OTHER;
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                SYS = PushConstant.BRAND_XIAOMI;//小米
            } else if (prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                    || prop.getProperty(KEY_EMUI_VERSION, null) != null
                    || prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null) {
                SYS = PushConstant.BRAND_HUAWEI;//华为
            } else if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                SYS = PushConstant.BRAND_MEIZU;//魅族
            }
            ;
        } catch (Exception e) {
            Log.e("SystemUtil", "Query brand failed. exception:" + e.getMessage(), e);
            return PushConstant.BRAND_OTHER;
        }
        return SYS;
    }

    public static String getMeizuFlymeOSFlag() {
        return getSystemProperty("ro.build.display.id", "");
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    /**
     * 手机均有一个唯一的标识(ID)
     *
     * @return
     */
    public String getIMEI(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            return CommonUtil.isEmpty(imei) ? "" : imei;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getLocale() {
        //获取系统当前使用的语言
        String lan = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        return (lan + "_" + country).toUpperCase();
    }

    public static String getLan() {
        //获取系统当前使用的语言
        String lan = Locale.getDefault().getLanguage();
        return TextUtils.isEmpty(lan) ? "" : lan.toUpperCase();
    }

//    /**
//     * 检测谷歌play服务是否可用, 推送必须使用
//     * @return
//     */
//    public static boolean isGooglePlayServicesAvailable(Context context) {
//        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
//        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
//        if(status != ConnectionResult.SUCCESS) {
//            // 弹出不能使用原因的提示框
////            if(googleApiAvailability.isUserResolvableError(status)) {
////                googleApiAvailability.getErrorDialog(context, status, 2404).show();
////            }
//            return false;
//        }
//        return true;
//    }
}
