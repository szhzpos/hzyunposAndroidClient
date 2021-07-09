package com.wyc.cloudapp.utils;

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
    public static String formatDataWithTimestamp(long t){
        return new java.text.SimpleDateFormat(YYYY_MM_DD_1, Locale.CHINA).format(t );
    }
    public static String formatCurrentTime(String pattern){
        return new java.text.SimpleDateFormat(pattern, Locale.CHINA).format(new Date());
    }
}
