package com.wyc.cloudapp.dialog.barcodeScales;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractBarcodeScaleImp implements IBarCodeScale {
    protected static final String  CHARACTER_SET = "GB2312";
    protected final static String CATEGORY_SEPARATE = ",";
    protected OnShowStatusCallback mCallback;

    @Override
    public void setShowStatus(OnShowStatusCallback o) {
        mCallback = o;
    }

    protected String get_zone_bit_code(final String content) throws UnsupportedEncodingException {
        String sz_code,sub_sz;
        StringBuilder zone_bit_code = new StringBuilder();
        byte[] bytes;
        for (int i = 0,length = content.length();i < length;i++){
            sub_sz = String.valueOf(content.charAt(i));
            bytes = sub_sz.getBytes("GB2312");
            if (check(sub_sz)){
                for (byte aByte : bytes) {
                    int a = Integer.parseInt(Utils.byteToHex(new byte[]{aByte}), 16);
                    sz_code =  String.valueOf((a - 0x80 - 0x20));
                    if (sz_code.length() == 1) {
                        sz_code = 0 + sz_code;
                    }
                    zone_bit_code.append(sz_code);
                }
            }else{
                int a = Integer.parseInt(Utils.byteToHex(new byte[]{bytes[0]}), 16);
                sz_code = String.valueOf(a - 0x20);
                if (sz_code.length() == 1) {
                    sz_code = 0 + sz_code;
                }
                zone_bit_code.append("03").append(sz_code);
            }
        }
        return zone_bit_code.toString();
    }

    private boolean check(String str)
    {
        final Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    static IBarCodeScale newInstance(final String class_name) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> scale_class = Class.forName("com.wyc.cloudapp.dialog.barcodeScales." + class_name);
        Constructor<?> constructor = scale_class.getConstructor();
        return  (IBarCodeScale) constructor.newInstance();
    }
    static List<Future<Boolean>> scaleDownLoad(@NonNull JSONArray array, final Map<String,TextView> map){
        List<Future<Boolean>> futureList = new ArrayList<>();
        for (int i = 0,size = array.size();i < size;i++){
            final JSONObject scales_info = array.getJSONObject(i);
            Future<Boolean> future = CustomApplication.submit(() -> {
                final TextView view = map.get(scales_info.getString("_id"));
                boolean code = false;
                try {
                    IBarCodeScale barcodeScale = newInstance(scales_info.getString("s_class_id"));
                    if (null != view) {
                        barcodeScale.setShowStatus(s -> CustomApplication.runInMainThread(() -> {
                            view.setText(s);
                        }));
                    }
                    code = barcodeScale.down(scales_info);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                    if (null != view) {
                        CustomApplication.runInMainThread(() -> view.setText(e.getMessage()));
                    }
                }
                return code;
            });
            futureList.add(future);
        }

        return futureList;
    }

    static JSONArray getManufacturerInfo(){
        final JSONArray array = new JSONArray();

        array.add(getDHManufacturer());
        array.add(getTldManufacturer());

        return array;
    }

    private static JSONObject getDHManufacturer(){
        JSONObject object = new JSONObject();
        JSONArray products = new JSONArray();

        object.put("name","大华系列");

        products.add(getScaleProduct(DH15A.class.getSimpleName(),"大华TM-15A"));

        object.put("products",products);

        return object;
    }

    private static JSONObject getTldManufacturer(){
        JSONObject object = new JSONObject();
        JSONArray products = new JSONArray();

        object.put("name","托利多系列");

        products.add(getScaleProduct(ToledoBPlus.class.getSimpleName(),"托利多bPlus"));

        object.put("products",products);

        return object;
    }

    private static JSONObject getScaleProduct(final String s_id, final String s_type){
        JSONObject object = new JSONObject();
        object.put("s_id",s_id);
        object.put("s_type",s_type);
        return object;
    }
}
