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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.wyc.cloudapp.dialog.MyDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import androidx.annotation.NonNull;

/**
 * Created by Administrator on 2018-03-06.
 */

public class Utils {
    public static void displayMessage(String message,Context context ){
        final MyDialog builder  = new	MyDialog(context);
        builder.setTitle("提示信息").setMessage(message).setNoOnclickListener("取消", new MyDialog.onNoOnclickListener() {
            @Override
            public void onNoClick(MyDialog myDialog) {
                myDialog.dismiss();
            }
        }).show();
    }

    public static void displayMessage(String message,String sz,Context context ){
        final MyDialog builder  = new	MyDialog(context);
        builder.setTitle("提示信息").setMessage(message).setNoOnclickListener(sz, new MyDialog.onNoOnclickListener() {
            @Override
            public void onNoClick(MyDialog myDialog) {
                myDialog.dismiss();
            }
        }).show();
    }
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
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F' };
        MessageDigest mdTemp = null;
        StringBuilder hexstr = new StringBuilder();
        try {
            mdTemp = MessageDigest.getInstance("MD5" );
            mdTemp.update(data);
            byte [] md = mdTemp.digest();
            for(int i = 0;i < md.length;i++){
/*            shaHex = Integer.toHexString(md[i] & 0xFF);
            if (shaHex.length() < 2) {
                hexstr.append(0);
            }*/
                hexstr.append(hexDigits[md[i] >>> 4 & 0x0f]);
                hexstr.append(hexDigits[md[i]  & 0x0f]);
            }
        } catch (NoSuchAlgorithmException | NullPointerException e) {
            hexstr.delete(0,hexstr.length());
            e.printStackTrace();
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

    public static Properties loadProperties(@NonNull Context context){
        FileOutputStream output = null;
        FileInputStream inputStream = null;
        Properties properties = null;
        try{
            inputStream = context.openFileInput("Properties.properties");//包名/files/文件名
            properties = new Properties();
            properties.load(inputStream);
        }catch (IOException ioe){
            if(ioe instanceof FileNotFoundException){
                try{
                    output = context.openFileOutput("Properties.properties",0);
                    properties = new Properties();
                    properties.setProperty("server","192.168.0.254");
                    properties.setProperty("port","1433");
                    properties.setProperty("user","sa");
                    properties.setProperty("password","sa");
                    properties.setProperty("database","hzpos_sy9");
                    properties.store(output,null);
                }catch (IOException e){
                    Utils.displayMessage(e.toString(),context);
                }finally {
                    try{
                        if (output != null) output.close();
                    }catch (IOException e){
                        Utils.displayMessage(e.toString(), context);
                    }finally {
                        output = null;
                    }
                }
            }else{
                Utils.displayMessage("加载配置文件出错" + ioe.toString(), context);
            }
        }finally {
            try{
                if (inputStream != null) inputStream.close();
            }catch (IOException e){
                Utils.displayMessage(e.toString(), context);
            }finally {
                inputStream = null;
            }
        }
        return  properties;
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
            return !isTrue;
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
        BigDecimal bg = new BigDecimal(d).setScale(scale, RoundingMode.UP);
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

    public  static String jsonToMd5(JSONObject json ,String apiKey) throws JSONException,UnsupportedEncodingException{
        Map<String,String> map = new HashMap<>();
        Map<String, String> sortMap = new TreeMap<String, String>();
        StringBuilder builder = new StringBuilder();
        String signStr = null;

        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
            String key = it.next();
            if("sign".equals(key))continue;
            map.put(key, json.getString(key));
        }
        sortMap.putAll(map);
        for (Map.Entry<String, String> s : sortMap.entrySet()) {
            String k = s.getKey();
            String v = s.getValue();
            if ("".equals(v)) {// 过滤空值
                continue;
            }
            if (builder.length() != 0){
                builder.append("&");
            }
            builder.append(k).append("=").append(v);
        }
        signStr = URLEncoder.encode(builder + "&key=" + apiKey,"UTF-8").replace("+", "%20");//"+" 替换空格
        return getMD5(signStr.getBytes());
    }

    public  static String jsonToMd5_hz(JSONObject json ,String apiKey) throws JSONException,UnsupportedEncodingException{
        Map<String,String> map = new HashMap<>();
        Map<String, String> sortMap = new TreeMap<String, String>();
        StringBuilder builder = new StringBuilder();
        String signStr = null;

        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
            String key = it.next();
            if("sign".equals(key))continue;
            map.put(key, json.getString(key));
        }
        sortMap.putAll(map);
        for (Map.Entry<String, String> s : sortMap.entrySet()) {
            String k = s.getKey();
            String v = s.getValue();
            /*if ("".equals(v)) {// 过滤空值
                continue;
            }*/
            if (builder.length() != 0){
                builder.append("&");
            }
            builder.append(k).append("=").append(v);
        }
        signStr = builder + apiKey;

        return builder.append("&sign=").append(getMD5(signStr.getBytes())).toString();
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
}
