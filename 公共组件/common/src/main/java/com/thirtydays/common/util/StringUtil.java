package com.thirtydays.common.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringUtil {
    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (null == str || str.trim().equals("") || str.trim().equalsIgnoreCase("null")) {
            return true;
        }
        return false;
    }

    /**
     * 替换、过滤特殊字符：解决排版混乱
     *
     * @param str
     */
    public static String stringFilter(String str) throws PatternSyntaxException {
        str = str.replaceAll("【", "[").replaceAll("】", "]").replaceAll("！", "!");//替换中文标号
        String regEx = "[『』]"; // 清除掉特殊字符
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);

        // textview中的字符全角化。即将所有的数字、字母及标点全部转为全角字符，使它们与汉字同占两个字节，这样就可以避免由于占位导致的排版混乱问题了
        char[] c = m.replaceAll("").trim().toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    /**
     * 格式化手机号码，加上超链接
     *
     * @param text
     * @return
     */
    public static String formatTelphone2Hyperlink(String text) {
        String commentContent = StringUtil.isEmpty(text) ? "" : text;
        ArrayList<String> phoneNumbers = findNumber(commentContent);
        if (CollectionUtil.isEmpty(phoneNumbers)) {
            return commentContent;
        } else {
            StringBuilder stringBuilder = new StringBuilder(text);
            int index;
            String number = null;
            for (int i = 0, size = phoneNumbers.size(); i < size; i++) {
                number = phoneNumbers.get(i);
                index = commentContent.indexOf(number);
                stringBuilder.replace(index, number.length() + index, "<a color=\"#529E84\" href=\"tel:" + number + "\">" + number + "</a>");
            }
            return stringBuilder.toString();
        }
    }


    public static ArrayList<String> findNumber(String str) {
        ArrayList<String> result = new ArrayList<String>();
        Pattern p = Pattern.compile("\\d{7,}");
        Matcher m = p.matcher(str);
        while (m.find()) {
            if (!isEmpty(m.group())) {
                result.add(m.group());
            }
        }
        return result;
    }


}
