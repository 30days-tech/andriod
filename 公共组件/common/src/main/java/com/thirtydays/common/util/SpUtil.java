package com.yanchenmeng.github;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by yanchengmeng on 2017/11/21.
 * SharedPreferences工具类
 */
public class SpUtil {


    private static final String TAG = SpUtil.class.getSimpleName();

    private static SharedPreferences mSharedPreferences;

    private static SpUtil mPreferencemManager;

    private static SharedPreferences.Editor editor;

    private Context context = null;

    private String cacheName;

    private SpUtil(Context cxt, String cacheName) {
        mSharedPreferences = cxt.getSharedPreferences(cacheName, Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        this.context = cxt;
        this.cacheName = cacheName;
    }

    public static synchronized void init(Context cxt, String cacheName) {
        if (mPreferencemManager == null) {
            mPreferencemManager = new SpUtil(cxt, cacheName);
        }
    }

    /**
     * 单例模式，获取instance实例
     *
     * @param
     * @return
     */
    public synchronized static SpUtil getInstance() {
        if (mPreferencemManager == null) {
            throw new RuntimeException("please init first!");
        }

        return mPreferencemManager;
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }


    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public float getFloat(String key, float defValue) {
        return mSharedPreferences.getFloat(key, defValue);
    }

    public void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }


    public long getLong(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    /**
     * 存储对象
     *
     * @param key
     * @param obj
     */
    public void put(String key, Object obj) {
        String objJsonStr = JsonUtil.obj2json(obj);
        editor.putString(key, objJsonStr);
        editor.commit();
    }

    /**
     * 获取对象
     *
     * @param key
     * @param clz
     * @param <T>
     * @return
     */
    public <T extends Object> T get(String key, Class clz) {
        String objStr = mSharedPreferences.getString(key, "");
        return (T) JsonUtil.json2obj(objStr, clz);
    }

    /**
     * 存储字符串集合
     *
     * @param key
     * @param set
     */
    public void putStringSet(String key, Set<String> set) {
        editor.putStringSet(key, set);
        editor.commit();
    }

    /**
     * 获取字符串集合
     *
     * @param key
     * @return
     */
    public Set<String> getStringSet(String key) {
        return mSharedPreferences.getStringSet(key, null);
    }

    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

}
