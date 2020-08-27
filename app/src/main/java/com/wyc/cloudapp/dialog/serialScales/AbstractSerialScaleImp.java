package com.wyc.cloudapp.dialog.serialScales;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android_serialport_api.SerialPort;

public abstract class AbstractSerialScaleImp implements ISerialScale {
    protected volatile boolean mReading = true;
    protected String mPort;
    protected SerialPort mSerialPort = null;
    protected OnReadStatus mOnReadStatus;

    @Override
    public synchronized void close(){
        if (mSerialPort != null){
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    public static int readWeight(final JSONObject object){
        int code = -1;
        if (SQLiteHelper.getLocalParameter("serial_port_scale",object)){
            final String cls_id = Utils.getNullStringAsEmpty(object,"cls_id"),ser_port = Utils.getNullOrEmptyStringAsDefault(object,"ser_port","NONE");
            if (!"NONE".equals(ser_port)){
                try {
                    Class<?> scale_class = Class.forName("com.wyc.cloudapp.dialog.serialScales." + cls_id);
                    Constructor<?> constructor = scale_class.getConstructor(String.class);
                    object.fluentClear().put("info",constructor.newInstance(ser_port));
                    code = 0;
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException |
                        InvocationTargetException e) {
                    code = -1;
                    e.printStackTrace();
                    object.fluentClear().put("info",e.getMessage());
                }
            }else{
                code = 1;
            }
        }
        return code;
    }

    public interface OnReadStatus {
        void onFinish(double num);
        void onError(final String err);
    }
    public ISerialScale setOnReadListener(OnReadStatus listener){
        mOnReadStatus = listener;
        return this;
    }
   public static JSONArray generateProductType(){
        final JSONArray array = new JSONArray();

        JSONObject object = new JSONObject();
        object.put("cls_id","DhSerialScale");
        object.put("name","大华ACS-A");
        array.add(object);

       object = new JSONObject();
       object.put("cls_id","DjSerialScale");
       object.put("name","顶尖ACS-OS2X");
       array.add(object);

        return array;
    }

}
