package com.thirtydays.pushservice.util;

import java.util.Collection;
import java.util.Map;

/**
 * Created by chenxiaojin on 2017/8/11.
 */

public class CommonUtil {
    public static boolean isEmpty(String str) {
        if (null == str || str.trim().equals("") || str.trim().equalsIgnoreCase("null")) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }


    public static boolean isEmpty(Map map) {
        return null == map || map.isEmpty();
    }
}
