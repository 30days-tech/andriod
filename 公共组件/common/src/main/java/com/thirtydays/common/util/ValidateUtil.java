package com.thirtydays.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenxiaojin on 2017/9/13.
 * 验证工具类
 */

public class ValidateUtil {

    /**
     * 检查手机号码是否合法
     *
     * @param mobileNumber 要检查的手机号码
     * @return true合法，false不合法
     */
    public static boolean isMobileNumber(String mobileNumber) {
        Pattern p = Pattern.compile("^((13)|(14)|(15)|(17)|(18))\\d{9}$");
        Matcher m = p.matcher(mobileNumber);
        return m.matches();
    }

    /**
     * 描述：是否是邮箱.
     *
     * @param str 指定的字符串
     * @return 是否是邮箱:是为true，否则false
     */
    public static boolean isEmail(String str) {
        Boolean isEmail = false;
        String expr = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        if (str.matches(expr)) {
            isEmail = true;
        }
        return isEmail;
    }

    /**
     * 判断是否符合身份证号码的规范
     * @param idCard 身份证号码
     * @return
     */
    public static boolean isIDCard(String idCard) {
        if (idCard != null) {
            String IDCardRegex = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x|Y|y)$)";
            return idCard.matches(IDCardRegex);
        }
        return false;
    }
}
