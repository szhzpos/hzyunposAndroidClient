package com.wyc.cloudapp.dialog.barcodeScales;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            sub_tmp = String.valueOf(content.charAt(i));
            bytes = sub_tmp.getBytes("GB2312");
            if (check(sub_tmp)){
                for (byte aByte : bytes) {
                    int a = Integer.parseInt(Utils.byteToHex(new byte[]{aByte}), 16);
                    t = (a - 0x80 - 0x20) + "";
                    if (t.length() == 1) {
                        t = 0 + t;
                    }
                    z_b_c.append(t);
                }
            }else{
                z_b_c.append(Integer.parseInt(Utils.byteToHex(bytes), 16));
            }
        }

        return z_b_c.toString();
    }

    private boolean check(String str)
    {
        final Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    protected boolean getBarcodePrefix(@NonNull final JSONObject object){
        return SQLiteHelper.getLocalParameter("scale_setting",object);
    }

    static boolean scaleDownLoad(@NonNull JSONObject scales_info, final TextView view){
        boolean code = false;
        try {
            Class<?> scale_class = Class.forName("com.wyc.cloudapp.dialog.barcodeScales." + scales_info.getString("s_class_id"));
            Constructor<?> constructor = scale_class.getConstructor();
            IBarCodeScale barcodeScale = (AbstractBarcodeScale)constructor.newInstance();
            if (null != view){
                barcodeScale.setUpdateStatus(s -> view.post(()->{
                    view.setText(s);
                }));
            }
            code = barcodeScale.down(scales_info);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            if (null != view){
                view.post(()->{
                    view.setText(e.getMessage());
                });
            }
        }
        return code;
    }

    static JSONObject getDHManufacturer(){
        JSONObject object = new JSONObject();
        JSONArray products = new JSONArray();

        object.put("name","大华系列");

        products.add(getScalseProduct("DH15A","大华TM-15A"));

        object.put("products",products);

        return object;
    }

    private static JSONObject getScalseProduct(final String s_id,final String s_type){
        JSONObject object = new JSONObject();
        object.put("s_id",s_id);
        object.put("s_type",s_type);
        return object;
    }
}
