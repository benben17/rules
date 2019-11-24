package com.spacex.rule.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {

    public static String getCurrentDate() {
        String dateStr = "";
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateStr = sdf.format(d);
        return dateStr;
    }

    public static String getDateStr(Long millis) {
        String dateStr = "";
        Date d = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateStr = sdf.format(d);
        return dateStr;
    }
}
