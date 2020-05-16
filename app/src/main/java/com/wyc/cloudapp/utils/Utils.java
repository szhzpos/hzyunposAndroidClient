package com.wyc.cloudapp.utils;
import android.app.Activity;
import android.content.Context;

import android.icu.text.SimpleDateFormat;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Base64;

import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.Random;

import java.util.UUID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.logger.Logger;

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
        return original.substring(start < 0 ? 0 :start,length);
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

    public static void hideKeyBoard(EditText v){
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
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
        BigDecimal bg = new BigDecimal(d).setScale(scale,BigDecimal.ROUND_HALF_UP );
        return bg.doubleValue();
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
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".")
        .append((ipInt >> 8) & 0xFF).append(".")
        .append((ipInt >> 16) & 0xFF).append(".")
        .append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    // IMEI码
    private static String getIMIEStatus(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        return deviceId;
    }

    // Mac地址
    private static String getLocalMac(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    // Android Id
    private static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    public static boolean JsonIsNotEmpty(final JSONObject json){
        return json != null && !json.isEmpty();
    }
    public static boolean JsonIsNotEmpty(final JSONArray jsons){
        return jsons != null && !jsons.isEmpty();
    }
    public static JSONObject JsondeepCopy(@NonNull final JSONObject jsonObject){
        return JSON.parseObject(jsonObject.toJSONString());
    }
    public static JSONArray JsondeepCopy(@NonNull final JSONArray jsons){
        return JSON.parseArray(jsons.toJSONString());
    }
    public static double getNotKeyAsNumberDefault(@NonNull final JSONObject object, final String key, double default_v){
        if (object.containsKey(key)){
            return object.getDoubleValue(key);
        }
        return default_v;
    }
    public static int getNotKeyAsNumberDefault(@NonNull final JSONObject object, final String key, int default_v){
        if (object.containsKey(key)){
            return object.getIntValue(key);
        }
        return default_v;
    }
    public static String getNullOrEmptyStringAsDefault(@NonNull final JSONObject object,final String key, final String default_v){
        final String value = object.getString(key);
        if (value != null && !"".equals(value)){
            return object.getString(key);
        }
        return default_v;
    }
    public static String getNullStringAsEmpty(@NonNull final JSONObject object,final String key){
        final String value = object.getString(key);
        return value == null ? "" :value;
    }

    public static JSONObject getNullObjectAsEmptyJson(final JSONObject object,final String key){
        if (object == null)return new JSONObject();
        final Object obj = object.get(key);
        if (obj instanceof JSONObject){
            return (JSONObject) obj;
        }
        return new JSONObject();
    }
    public static JSONArray getNullObjectAsEmptyJsonArray(final JSONObject object,final String key){
        if (object == null)return new JSONArray();
        final Object obj = object.get(key);
        if (obj instanceof JSONArray){
            return (JSONArray) obj;
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

    public static void  disableView(final View v,final long mill){
        if (v != null){
            v.setEnabled(false);
            v.postDelayed(()->v.setEnabled(true),mill);
        }
    }

    public static boolean equalDouble(double a,double b){
        return Math.abs(a - b) < 0.00001;
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
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
