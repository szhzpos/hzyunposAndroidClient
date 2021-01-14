package com.wyc.cloudapp.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.logger.Logger;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Administrator on 2018-03-06.
 */

public final class Utils {
    public static String getDeviceId(Context context) {
        String deviceId = getLocalMac(context).replace(":", "") + getAndroidId(context);
         if ("".equals(deviceId)) {
            UUID uuid = UUID.randomUUID();
            deviceId = uuid.toString().replace("-", "");
        }
        deviceId = getMD5(deviceId.getBytes());
        return deviceId.substring(0,16);
    }

    public  static  String getIMIE_string(Context context){
        String deviceId = getMD5(getIMIEStatus(context).getBytes());
        return deviceId.substring(0,16);
    }

    public static String getFirstSpell(String chinese) {
        StringBuilder pybf = new StringBuilder();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char curchar : arr) {
            if (curchar > 128) {
                try {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(curchar, defaultFormat);
                    if (temp != null) {
                        pybf.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pybf.append(curchar);
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim().toUpperCase();
    }

    public static Map<String,String> parseXml(Reader xmlStream) throws XmlPullParserException,IOException {
        Map<String,String> map = new HashMap<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(xmlStream);
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (!"leshua".equals(xpp.getName())){
                        map.put(xpp.getName(),xpp.nextText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                case XmlPullParser.TEXT:
                    break;
            }
            eventType = xpp.next();
        }
        return map;
    }

    @NonNull
    public static String getMD5(byte[] data) {
        MessageDigest mdTemp = null;
        try {
            mdTemp = MessageDigest.getInstance("MD5" );
            mdTemp.update(data);
            byte [] md = mdTemp.digest();
            return byteToHex(md);
        } catch (NoSuchAlgorithmException | NullPointerException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String byteToHex(byte[] data){
        StringBuilder hexstr = new StringBuilder();
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','A', 'B', 'C', 'D', 'E', 'F' };
        for (byte datum : data) {
            hexstr.append(hexDigits[datum >>> 4 & 0x0f]);
            hexstr.append(hexDigits[datum & 0x0f]);
        }
        return hexstr.toString();
    }

    public  static  String getMD5ToBase64(byte[] data){
        byte [] md = null;
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5" );
            mdTemp.update(data);
            md = mdTemp.digest();
        }catch (NoSuchAlgorithmException | NullPointerException e){
            e.printStackTrace();
            return null;
        }
        return Base64.encodeToString(md,Base64.DEFAULT );
    }

    public static String substringFormRight(final String original,int count){
        //count 从右边开始字符数
        if (null == original || original.isEmpty()){
            return "";
        }
        int length = original.length();
        int start =length  - count;
        return original.substring(Math.max(start, 0),length);
    }

    public static void setFocus(Activity activity, EditText textView){
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.requestFocus();
        textView.selectAll();
        InputMethodManager imm = (InputMethodManager)textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(textView,0);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void setFocus(Activity activity, EditText textView,boolean isfocus){
        if (isfocus){
            hideKeyBoard(textView);
        }
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.requestFocus();
        textView.selectAll();
    }

    public static void hideKeyBoard(final EditText v){
        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            int currentVersion = android.os.Build.VERSION.SDK_INT;
            String methodName = null;
            if (currentVersion >= 16) {
                // 4.2
                methodName = "setShowSoftInputOnFocus";
            } else if (currentVersion >= 14) {
                // 4.0
                methodName = "setSoftInputShownOnFocus";
            }
            if (methodName == null) {
                v.setInputType(InputType.TYPE_NULL);
            } else {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                try {
                    setShowSoftInputOnFocus = cls.getMethod(methodName,
                            boolean.class);
                    setShowSoftInputOnFocus.setAccessible(true);
                    setShowSoftInputOnFocus.invoke(v, false);
                } catch (NoSuchMethodException e) {
                    v.setInputType(InputType.TYPE_NULL);
                    e.printStackTrace();
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean verifyDate(String sz){
        boolean isTrue = true;
        if (sz.length() != 10){
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
        try {
            format.setLenient(false);
            format.parse(sz);
        }catch (ParseException e){
            isTrue = false;
        }
        return isTrue;
    }

    public static double formatDouble(double d,int scale) {
        // 新方法，如果不需要四舍五入，可以使用RoundingMode.DOWN
        return BigDecimal.valueOf(d).setScale(scale,BigDecimal.ROUND_HALF_UP ).doubleValue();
    }

    public static double formatDoubleDown(double d,int scale) {
        // 新方法，如果不需要四舍五入，可以使用RoundingMode.DOWN
        return BigDecimal.valueOf(d).setScale(scale, RoundingMode.DOWN ).doubleValue();
    }

    public static String getNonce_str(int length) {
        String SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] nonceChars = new char[SYMBOLS.length() < length ? SYMBOLS.length():length];
        Random random = new Random();

        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = SYMBOLS.charAt(random.nextInt(SYMBOLS.length()));
        }
        return new String(nonceChars);
    }

    public static String getNonce_num(int length) {
        String SYMBOLS = "0123456789";
        char[] nonceChars = new char[SYMBOLS.length() < length ? SYMBOLS.length():length];
        Random random = new Random();

        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = SYMBOLS.charAt(random.nextInt(SYMBOLS.length()));
        }
        return new String(nonceChars);
    }

    public static String intToIp(int ipInt) {
        final StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".")
        .append((ipInt >> 8) & 0xFF).append(".")
        .append((ipInt >> 16) & 0xFF).append(".")
        .append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    // IMEI码
    private static String getIMIEStatus(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm)
            return tm.getDeviceId();
        else
            return "";
    }

    // Mac地址
    private static String getLocalMac(Context context) {
        final WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null)
            return wifi.getConnectionInfo().getMacAddress();
        else
            return "";
    }

    // Android Id
    private static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean JsonIsNotEmpty(final JSONObject json){
        return json != null && !json.isEmpty();
    }
    public static boolean JsonIsNotEmpty(final JSONArray jsons){
        return jsons != null && !jsons.isEmpty();
    }
    public static JSONObject JsondeepCopy(@Nullable final JSONObject jsonObject){
        if (jsonObject == null)return new JSONObject();
        return JSON.parseObject(jsonObject.toJSONString());
    }
    public static JSONArray JsondeepCopy(@Nullable final JSONArray jsons){
        if (jsons == null)return new JSONArray();
        return JSON.parseArray(jsons.toJSONString());
    }

    public static String getNullOrEmptyStringAsDefault(@Nullable final JSONObject object,final String key, final String default_v){
        if (object != null){
            final String value = object.getString(key);
            if (value != null && !"".equals(value)){
                return value;
            }
        }
        return default_v;
    }
    public static double getNotKeyAsNumberDefault(@Nullable final JSONObject object,final String key, final double default_v){
        if (object != null){
            final Double obj = object.getDouble(key);
            if (null != obj)return obj;
        }
        return default_v;
    }
    public static int getNotKeyAsNumberDefault(@Nullable final JSONObject object,final String key, final int default_v){
        if (object != null){
            final Integer obj = object.getInteger(key);
            if (null != obj)return obj;
        }
        return default_v;
    }

    public static <T> T getNotKeyAsNumberDefault(@Nullable final JSONObject object, final String key, final T default_v){
        if (object != null){
            final T obj = (T) object.get(key);
            if (null != obj)return obj;
        }
        return default_v;
    }

    public static String getNullStringAsEmpty(@Nullable final JSONObject object,final String key){
        if (null != object){
            final String value = object.getString(key);
            return value == null ? "" :value;
        }
        return "";
    }

    public static JSONObject getNullObjectAsEmptyJson(final JSONObject object,final String key){
        if (object != null){
            final Object obj = object.get(key);
            if (obj instanceof JSONObject){
                return (JSONObject) obj;
            }
        }
        return new JSONObject();
    }
    public static JSONArray getNullObjectAsEmptyJsonArray(final JSONObject object,final String key){
        if (object != null){
            final Object obj = object.get(key);
            if (obj instanceof JSONArray){
                return (JSONArray) obj;
            }
        }
        return new JSONArray();
    }

    public static int getViewTagValue(final View view,int default_V){
        Object tag;
        if (view != null && (tag = view.getTag()) != null){
            if (tag instanceof Integer){
                default_V = (int)tag;
            }
        }
        return default_V;
    }
    public static String getViewTagValue(final View view,String default_V){
        Object tag;
        if (view != null && (tag = view.getTag()) != null){
            if (tag instanceof String){
                default_V = (String) tag;
            }
        }
        return default_V;
    }

    public static JSONObject getViewTagValue(final View view){
        Object tag;
        if (view != null && (tag = view.getTag()) != null){
            if (tag instanceof JSONObject){
                return (JSONObject) tag;
            }
        }
        return new JSONObject();
    }

    public static String getUserIdAndPasswordCombinationOfMD5(final String content){
        return Utils.getMD5((content + "hzyunpos").getBytes());
    }
    public static void deleteFile(final File file) throws IOException {
        if (file.isFile()){
            if (!file.delete())throw new IOException(String.format(Locale.CHINA,"delete file fi:%s failed",file.getName()));
        }else {
            final File[] files = file.listFiles();
            if (files != null){
                for (File f : files){
                    deleteFile(f);
                }
            }
        }

    }
    public static void moveJsonArray(final JSONArray from,final JSONArray to){
        if (from != null && to != null){
            Object o;
            while (!from.isEmpty() && null != (o = from.remove(0))){
                to.add(o);
            }
        }
    }

    public static void sortJsonArrayFromDoubleCol(@NonNull final JSONArray array,final String col){
        int size = array.size();
        JSONObject pre,next;
        for (int i = 0;i < size;i++){
            for (int j = 0;j < size - i -1;j++){
                pre = array.getJSONObject(j);
                next = array.getJSONObject(j + 1);
                if (pre.getDoubleValue(col) > next.getDoubleValue(col)){
                    pre = (JSONObject) array.remove(j);
                    next = (JSONObject) array.remove(j);
                    array.add(j,next);
                    array.add(j + 1,pre);
                }
            }
        }
    }

    public static void bubbling_sort(int[] arr){
        int size = arr.length;
        long start_time = System.currentTimeMillis();
        for (int i = 0;i < size;i++){
            for (int j = 0;j < size - i -1;j++){
                int key = arr[j];
                if(key > arr[j + 1]){
                    arr[j] = arr[j + 1];
                    arr[j + 1] = key;
                }
            }
        }
        Logger.d("end_time:%d",System.currentTimeMillis() - start_time);
    }

    public static void insertion_sort(int[] arr){
        long start_time = System.currentTimeMillis();
        for (int i = 1,size = arr.length;i < size;i ++){
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key){
                arr[j + 1] = arr[j];
                j = j -1;
            }
            arr[j + 1] = key;
        }
        Logger.d("end_time:%d",System.currentTimeMillis() - start_time);
    }

    public static void merge_sort(int[] data, int start, int end){
        int mid = (start+end)/2;
        if(start < end){
            merge_sort(data,start,mid);
            merge_sort(data,mid+1,end);
            merge(data,start,mid,end);
        }
    }

    private static void merge(int[] data, int start, int mid, int end){
        int p = start, q = mid+1, r = 0;
        int[] newdata = new int[end-start+1];
        while(p <= mid && q <= end){
            if(data[p] <= data[q]){                 //从大到小排序
                newdata[r++] = data[p++];
            }else{
                newdata[r++] = data[q++];
            }
        }

        //此时，两个子数组中会有一个中元素还未被全部归并到新数组中，作如下处理
        while(p <= mid){
            newdata[r++] = data[p++];
        }
        while(q <= end){
            newdata[r++] = data[q++];
        }
        //再将有序的数组中的值赋给原数组，其实也可以直接返回这个新数组
        int length = end + 1 - start;
        if (length >= 0)System.arraycopy(newdata, 0, data, start, length);
    }


    public static void  disableView(final View v,final long mill){
        if (v != null){
            v.setEnabled(false);
            v.postDelayed(()->v.setEnabled(true),mill);
        }
    }

    public static boolean equalDouble(double a,double b){
        return Math.abs(a - b) < 0.00001;
    }


    public static void showTimePickerDialog(final Context context, final TextView tv, Calendar calendar) {
        new TimePickerDialog( context,3,
                // 绑定监听器
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tv.setText(String.format(Locale.CHINA,"%02d:%02d:%02d",hourOfDay,minute,0));
                    }
                }
                // 设置初始时间
                , calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE)
                // true表示采用24小时制
                ,true).show();
    }

    public static void showDatePickerDialog(final Context context, final TextView tv, Calendar calendar) {
        new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tv.setText(String.format(Locale.CHINA,"%d-%02d-%02d",year,monthOfYear + 1,dayOfMonth));
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                ,calendar.get(Calendar.MONTH)
                ,calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    public static String unicodeToString(String unicode) {
        StringBuilder sb = new StringBuilder();
        String[] hex = unicode.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            int index = Integer.parseInt(hex[i], 16);
            sb.append((char) index);
        }
        return sb.toString();
    }

    public static StringBuilder unicode2StringWithStringBuilder(@NonNull final StringBuilder unicode) {
         Pattern patternUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
        Matcher matcher = patternUnicode.matcher(unicode);
        int offset = 0; //StringBuilder替换长度不等的字符产生的位置偏移
        String current,code,ch;
        while (matcher.find()) {
            current = matcher.group();
            code = matcher.group(1);
            if (code != null){
                ch = String.valueOf((char) Integer.parseInt(code, 16));
                unicode.replace(matcher.start() + offset, matcher.end() + offset, ch);
                offset += 1 - current.length(); //1为ch长度
            }
        }
        return unicode;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dpToPx(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(final Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int sp2px(final Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(final Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static long factorial(int n){
        if (n < 0)return 0;
        if (n == 0) return 1;
        return n * factorial(n - 1);
    }

    public static double getDisplaySize(@NonNull final Context context){
        final SharedPreferences preferences = context.getSharedPreferences("display_size", Context.MODE_PRIVATE);
        int size = preferences.getInt("size",-1);
        if (size == -1)return getDisplayMetrics(context,null);
        return size;
    }

    public static double getDisplayMetrics(@NonNull final Context context,DisplayMetrics displayMetrics){
        final WindowManager wm = (WindowManager)context.getSystemService(WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        if (displayMetrics == null)displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        float w = displayMetrics.widthPixels / displayMetrics.xdpi,h = displayMetrics.heightPixels / displayMetrics.ydpi;
        double diagonal = Math.sqrt(w * w + h * h);
        Logger.d("displayMetrics:%s,diagonal:%f",displayMetrics,diagonal);
        return diagonal;
    }

    public static boolean lessThan7Inches(@NonNull final Context context){
        return  Utils.getDisplaySize(context) < 7.0;
    }

    public static int getStatusBarHeight(Context context) {
        final Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("  at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
