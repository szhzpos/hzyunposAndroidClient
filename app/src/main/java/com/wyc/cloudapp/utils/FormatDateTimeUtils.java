package com.wyc.cloudapp.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.utils
 * @ClassName: FormatDataTime
 * @Description: 格式时间
 * @Author: wyc
 * @CreateDate: 2021-07-09 19:01
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-09 19:01
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class FormatDateTimeUtils {
    public static final String YYYY_MM_DD_1 = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_2 = "yyyy-MM-dd";
    public static String formatTimeWithTimestamp(Long t){
        return new java.text.SimpleDateFormat(YYYY_MM_DD_1, Locale.CHINA).format(t);
    }
    public static String formatDate(Long t){
        return new java.text.SimpleDateFormat(YYYY_MM_DD_2, Locale.CHINA).format(t);
    }
    public static String formatCurrentTime(String pattern){
        return new java.text.SimpleDateFormat(pattern, Locale.CHINA).format(new Date());
    }

    public static String formatDate(String pattern,long time){
        return new java.text.SimpleDateFormat(pattern, Locale.CHINA).format(new Date());
    }

    public static void setStartTime(final Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
    }
    public static void setEndTime(final Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
    }
}
