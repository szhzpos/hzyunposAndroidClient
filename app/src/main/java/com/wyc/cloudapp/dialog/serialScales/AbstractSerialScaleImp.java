package com.wyc.cloudapp.dialog.serialScales;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.IOException;
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
        if (hasSettingSerialPortScale(object)){
            final String cls_id = Utils.getNullStringAsEmpty(object,"cls_id"),ser_port = Utils.getNullOrEmptyStringAsDefault(object,"ser_port","NONE");
            try {
                Class<?> scale_class = Class.forName("com.wyc.cloudapp.dialog.serialScales." + cls_id);
                Constructor<?> constructor = scale_class.getConstructor(String.class);
                object.fluentClear().put("info",constructor.newInstance(ser_port));
                code = 0;
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException |
                    InvocationTargetException e) {
                e.printStackTrace();
                object.fluentClear().put("info",e.getMessage());
            }
        }else{
            code = 1;
        }
        return code;
    }
    public static boolean hasSettingSerialPortScale(@Nullable JSONObject object){
        if (null == object)object = new JSONObject();
        if (SQLiteHelper.getLocalParameter("serial_port_scale",object)){
            return !"NONE".equals(Utils.getNullOrEmptyStringAsDefault(object,"ser_port","NONE"));
        }
        return false;
    }
    public static boolean hasAutoGetWeigh(){
        final JSONObject object = new JSONObject();
        boolean code = hasSettingSerialPortScale(object);
        return code && object.getBooleanValue("auto_weigh");
    }

    public interface OnReadStatus {
        int STABLE = 0;
        int NO_STABLE = 1;
        int OTHER = -1;
        /**
         * stat 0稳定 1不稳定 -1其他
         * */
        void onFinish(int stat,double num);
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

       object = new JSONObject();
       object.put("cls_id","ToledoScale");
       object.put("name","托利多 Plus U2");
       array.add(object);

        return array;
    }

    @Override
    public void write(byte[] c) {
        if (mSerialPort != null){
            try {
                mSerialPort.getOutputStream().write(c);
                MyDialog.toastMessage(CustomApplication.self().getString(R.string.success));
            } catch (IOException e) {
                e.printStackTrace();
                MyDialog.toastMessage(e.getMessage());
            }
        }
    }
    public void rZero(){
        write(new byte[]{0x3C,0x5A,0x4B,0x3E,0x09});
    }
    public void tare(){
        write(new byte[]{0x3C,0x54,0x4B,0x3E,0x09});
    }
}
