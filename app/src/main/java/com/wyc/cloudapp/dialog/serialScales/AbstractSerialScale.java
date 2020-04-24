package com.wyc.cloudapp.dialog.serialScales;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android_serialport_api.SerialPort;

public abstract class AbstractSerialScale implements ISerialScale {
    volatile boolean mReading = true;
    SerialPort mSerialPort;
    OnReadStatus mOnReadStatus;
    public static int readWeight(final JSONObject object){
        int code = -1;
        AbstractSerialScale serialScale = null;
        if (SQLiteHelper.getLocalParameter("serial_port_scale",object)){
            final String cls_id = Utils.getNullStringAsEmpty(object,"cls_id"),
                    ser_port = Utils.getNullOrEmptyStringAsDefault(object,"ser_port","NONE");
            if (!"NONE".equals(ser_port)){
                try {
                    Class<?> scale_class = Class.forName("com.wyc.cloudapp.dialog.serialScales." + cls_id);
                    Constructor<?> constructor = scale_class.getConstructor();
                    serialScale = (AbstractSerialScale)constructor.newInstance();
                    serialScale.init(ser_port);
                    serialScale.startRead();
                    object.fluentClear().put("info",serialScale);
                    code = 0;
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | IOException | SecurityException e) {
                    code = -1;
                    e.printStackTrace();
                    if (serialScale != null){
                        serialScale.stopRead();
                    }
                    object.fluentClear().put("info",e.getMessage());
                }
            }else{
                code = 1;
            }
        }
        return code;
    }

    private void init(String port) throws IOException,SecurityException  {
        mSerialPort = new SerialPort(new File(port), 9600, 0);
    }

    public void setOnReadFinish(OnReadStatus listener){
        mOnReadStatus = listener;
    }

   public static JSONArray generateProductType(){
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();


        object.put("cls_id","DhSerialScale");
        object.put("name","大华ACS-A");
        array.add(object);


        return array;
    }

}
