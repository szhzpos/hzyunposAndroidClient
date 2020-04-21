package com.wyc.cloudapp.dialog.barcodeScales;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractBarcodeScale implements IBarCodeScale {
    final static String CATEGORY_SEPARATE = ",";

    @Override
    public boolean parse() {
        return false;
    }

    String get_zone_bit_code(final String content) throws UnsupportedEncodingException {
        String t,sub_tmp;
        StringBuilder z_b_c = new StringBuilder();
        byte[] bytes;
        for (int i = 0,length = content.length();i < length;i++){
            sub_tmp = content.substring(i,i+1);
            bytes = sub_tmp.getBytes("GB2312");
            for (byte aByte : bytes) {
                int a = Integer.parseInt(Utils.byteToHex(new byte[]{aByte}), 16);
                t = (a - 0x80 - 0x20) + "";
                if (t.length() == 1) {
                    t = 0 + t;
                }
                z_b_c.append(t);
            }
        }
        return z_b_c.toString();
    }
    public static boolean check(String fstrData) {
        char c = fstrData.charAt(0);
        if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
            return true;
        } else {
            return false;
        }
    }

    static boolean scaleDownLoad(JSONObject scales_info,final StringBuilder err){
        boolean code = false;
        if (null != scales_info){
            String class_id = scales_info.optString("s_class_id");
            try {
                Class<?> scale_class = Class.forName("com.wyc.cloudapp.dialog.barcodeScales." + class_id);
                Constructor<?> constructor = scale_class.getConstructor();
                IBarCodeScale iBarCodeScale = (IBarCodeScale)constructor.newInstance();
                code = iBarCodeScale.down(scales_info,err);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                if (null != err)err.append(e.getMessage());
            }
        }else{
            if (err != null)err.append("秤信息不能为空！");
        }
        return code;
    }

    static JSONObject getDHManufacturer() throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray products = new JSONArray();

        object.put("name","大华系列");

        products.put(getScalseProduct("DH15A","大华TM-15A"));

        object.put("products",products);

        return object;
    }

    private static JSONObject getScalseProduct(final String s_id,final String s_type) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("s_id",s_id);
        object.put("s_type",s_type);
        return object;
    }
}
