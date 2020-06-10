package com.wyc.cloudapp.print;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.VipChargeDialogImp;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import android_serialport_api.SerialPort;

public final class Printer {
    private static final String  CHARACTER_SET = "GB2312";
    public static final byte[][] byteCommands = {
            { 0x1b, 0x4d, 0x00 },// 标准ASCII字体
            { 0x1b, 0x4d, 0x01 },// 压缩ASCII字体
            { 0x1b, 0x7b, 0x00 },// 取消倒置打印
            { 0x1b, 0x7b, 0x01 },// 选择倒置打印
            { 0x1d, 0x42, 0x00 },// 取消黑白反显
            { 0x1d, 0x42, 0x01 },// 选择黑白反显
            { 0x1b, 0x56, 0x00 },// 取消顺时针旋转90°
            { 0x1b, 0x56, 0x01 },// 选择顺时针旋转90°
    };


    /**
     * 复位打印机
     */
    public static final byte[] RESET = {0x1b, 0x40};

    /**
     * 字符模式
     */
    public static final byte[] CHARACTER_MODE= {0x1b, 0x21,0};

    /**
     * 左对齐
     */
    public static final byte[] ALIGN_LEFT = {0x1b, 0x61, 0x00};

    /**
     * 中间对齐
     */
    public static final byte[] ALIGN_CENTER = {0x1b, 0x61, 0x01};

    /**
     * 右对齐
     */
    public static final byte[] ALIGN_RIGHT = {0x1b, 0x61, 0x02};

    /**
     * 选择加粗模式
     */
    public static final byte[] BOLD = {0x1b, 0x45, 0x01};

    /**
     * 取消加粗模式
     */
    public static final byte[] BOLD_CANCEL = {0x1b, 0x45, 0x00};

    /**
     * 宽高加倍
     */
    public static final byte[] DOUBLE_HEIGHT_WIDTH = {0x1d, 0x21, 0x11};

    /**
     * 宽加倍
     */
    public static final byte[] DOUBLE_WIDTH = {0x1d, 0x21, 0x10};

    /**
     * 高加倍
     */
    public static final byte[] DOUBLE_HEIGHT = {0x1d, 0x21, 0x01};

    /**
     * 字体不放大
     */
    public static final byte[] NORMAL = {0x1d, 0x21, 0x00};

    /**
     * 设置行间距
     */
    public static final byte[] LINE_SPACING_DEFAULT = {0x1b, 0x32};
    public static final byte[] LINE_SPACING_8 = {0x1b,0x33};
    public static final byte[] LINE_SPACING_4 = {0x1b,0x33,0x04};
    public static final byte[] LINE_SPACING_2 = {0x1b,0x33,0x02};
    public static final byte[] LINE_SPACING_16 = {0x1b,0x33,0x59};
    /**
     * 换行
     */
    public static final byte[] NEW_LINE  = {0x0A, 0x0D};

    /**
     * 退纸
     */
    public static final byte[] BACK_STEP  = {0x1B, 0x6A};

    /**
     * 开钱箱
     */
    public static final byte[] OPEN_CASHBOX  = {0x1B, 0x70,0x0,0x3c,0x79};

    /**
     * 打印纸一行最大的字节
     */
    private static final int LINE_BYTE_SIZE = 32;

    /**
     * 打印三列时，中间一列的中心线距离打印纸左侧的距离
     */
    private static final int LEFT_LENGTH = 16;

    /**
     * 打印三列时，中间一列的中心线距离打印纸右侧的距离
     */
    private static final int RIGHT_LENGTH = 16;

    /**
     * 打印三列时，第一列汉字最多显示几个文字
     */
    private static final int LEFT_TEXT_MAX_LENGTH = 8;

    public static void set_line_spacing(OutputStream writer, int n) throws IOException{
        writer.write(new byte[]{0x1B,0x33});
        writer.write(n);
        writer.flush();
    }
    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

    public static String printTwoData(int align,String leftText, String rightText) {
        StringBuilder sb = new StringBuilder();
        int leftTextLength = getBytesLength(leftText);
        int rightTextLength = getBytesLength(rightText);
        sb.append(leftText);

        // 计算两侧文字中间的空格
        int marginBetweenMiddleAndRight = LINE_BYTE_SIZE - leftTextLength - rightTextLength;
            if (align == 1)
                for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
                    sb.append(" ");
                }
                else
                sb.append(" ");

        sb.append(rightText);
        return sb.toString();
    }

    public static String printThreeData(int space,String leftText, String middleText, String rightText) {
        StringBuilder sb = new StringBuilder();
        // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
        if (leftText.length() > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + "..";
        }
        int leftTextLength = getBytesLength(leftText);
        int middleTextLength = getBytesLength(middleText);
        int rightTextLength = getBytesLength(rightText);

        sb.append(leftText);
        // 计算左侧文字和中间文字的空格长度
        int marginBetweenLeftAndMiddle = LEFT_LENGTH - space / 2 - leftTextLength - middleTextLength / 2;
        if (marginBetweenLeftAndMiddle > 0) {
            for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
                sb.append(" ");
            }
        }else {
            sb.append(" ").append(" ");
        }
        sb.append(middleText);

        // 计算右侧文字和中间文字的空格长度
        int marginBetweenMiddleAndRight = RIGHT_LENGTH - space / 2 - middleTextLength / 2 - rightTextLength;
        if (marginBetweenMiddleAndRight > 0) {
            for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
                sb.append(" ");
            }
        }else {
            sb.append(" ").append(" ");
        }
        sb.append(rightText);
        return sb.toString();
    }

    public static String printThreeDataAlignRight_58(int space, String leftText, String middleText, final String rightText) {
        StringBuilder sb = new StringBuilder();
        final String spacing = " ";
        final int mid_right_margin = 10 - space,left_margin = 12;
        int left_byte_size = getBytesLength(leftText);
        if (left_byte_size > left_margin) {
            leftText = leftText.substring(0, left_margin / 2) + "..";
            sb.append(leftText).append(spacing);
        }else {
            sb.append(leftText);
            for (int i = 0,k = left_margin - left_byte_size; i <= k ; i++) {
                sb.append(spacing);
            }
        }

        // 计算左侧文字和中间文字的空格长度
        int mid_byte_size = getBytesLength(middleText);
        if (mid_byte_size < mid_right_margin) {
            for (int i = 0; i <= mid_right_margin - mid_byte_size; i++) {
                sb.append(spacing);
            }
            sb.append(middleText);
        }else {
            sb.append(middleText.substring(0,mid_right_margin));
        }

        int rig_byte_size = getBytesLength(rightText);
        if (rig_byte_size < mid_right_margin) {
            for (int i = 0; i <= mid_right_margin - rig_byte_size; i++) {
                sb.append(spacing);
            }
            sb.append(rightText);
        }else {
            sb.append(rightText.substring(0,mid_right_margin));
        }

        return sb.toString();
    }

    public static String commandToStr(byte[] bytes){
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    public static void print(@NonNull final Activity context, @NonNull final String content){
        if (content.isEmpty())return;

        try {
            print(context,content.getBytes(CHARACTER_SET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void print(@NonNull final Activity context, @NonNull final byte[] inbyte){
        if (inbyte.length == 0)return;

        final JSONObject object = new JSONObject();
        if (SQLiteHelper.getLocalParameter("printer",object)){
            int status_id = object.getIntValue("id");
            String tmp = Utils.getNullStringAsEmpty(object,"v");
            String[] vals = tmp.split("\t");
            if (vals.length > 1){
                switch (status_id){
                    case R.id.bluetooth_p:
                        bluetooth_print(context,inbyte,vals[1]);
                        break;
                    case R.id.usb_p:
                        usb_print_byte(context,vals[0].substring(vals[0].indexOf(":") + 1),vals[1].substring(vals[1].indexOf(":") + 1),inbyte);
                        break;
                }
            }
        }else {
            MyDialog.ToastMessage("读取打印机设置错误：" + object.getString("info"),context,null);
        }
    }

    private static void bluetooth_print(@NonNull final Activity context,final  byte[] content,final String device_addr){
        if(content != null && device_addr != null){
            CustomApplication.execute(()->{
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null){
                    if (bluetoothAdapter.isEnabled()){
                        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device_addr);
                        synchronized (Printer.class){
                            try (BluetoothSocket bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                 OutputStream outputStream = bluetoothSocket.getOutputStream();){

                                bluetoothSocket.connect();

                                outputStream.write(RESET);

                                byte[] tmpBytes;
                                int length = content.length,max_length = 2048;
                                int count = length / max_length,tmp_c = 0,mod_length = 0;

                                if (count == 0){
                                    outputStream.write(content);
                                }else{
                                    if ((mod_length = length % max_length) > 0)count += 1;
                                    while (tmp_c < count){
                                        if (tmp_c + 1 == count){
                                            tmpBytes = Arrays.copyOfRange(content,tmp_c * max_length,tmp_c * max_length + mod_length);
                                        }else
                                            tmpBytes = Arrays.copyOfRange(content,tmp_c * max_length,tmp_c * max_length + max_length);

                                        outputStream.write(tmpBytes);
                                        tmp_c++;
                                    }
                                }
                                outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                context.runOnUiThread(()->MyDialog.ToastMessage("打印错误：" + e.getMessage(),context,null));
                            }
                        }
                    }else{
                        context.runOnUiThread(()->MyDialog.ToastMessage("蓝牙已关闭！",context,null));
                    }
                }
            });
        }
    }

    private static void usb_print_byte(@NonNull final Activity context,final String vid,final String pid,final byte[] in_bytes){
        CustomApplication.execute(()->{
            UsbDevice device = null;
            UsbInterface usbInterface = null;
            UsbEndpoint usbOutEndpoint = null,usbInEndpoint = null,tmpEndpoint;
            UsbDeviceConnection connection = null;
            UsbManager manager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
            if (manager != null){
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                for(String sz:deviceList.keySet()){
                    device = deviceList.get(sz);
                    if (device != null){
                        Logger.d("name:%s,--vid:%s,--pid:%s",device.getDeviceName(),vid,pid);
                        Logger.d("name:%s,vid:%d,pid:%d",device.getDeviceName(),device.getVendorId(),device.getProductId());
                        if (String.valueOf(device.getVendorId()).equals(vid) && String.valueOf(device.getProductId()).equals(pid)){
                            break;
                        }
                    }
                }
                if (null != device){
                    usbInterface = device.getInterface(0);
                    for(int i = 0,size = usbInterface.getEndpointCount();i < size; i++){
                        tmpEndpoint = usbInterface.getEndpoint(i);
                        if (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_OUT){
                            usbOutEndpoint = tmpEndpoint;
                        }else if (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_IN){
                            usbInEndpoint =tmpEndpoint;
                        }
                    }
                    if (usbOutEndpoint != null){
                        connection = manager.openDevice(device);
                        if (null != connection){
                            if (connection.claimInterface(usbInterface, true)){
                                try {

                                    byte[] tmpBytes;
                                    int length = in_bytes.length,max_length = 4096;
                                    int count = length / max_length,tmp_c = 0,ret_c = 0,mod_length = 0;

                                    synchronized (Printer.class){
                                       connection.bulkTransfer(usbOutEndpoint,RESET,RESET.length, 100);

                                        if (count == 0){
                                            ret_c = connection.bulkTransfer(usbOutEndpoint,in_bytes,length, 30000);
                                        }else{
                                            if ((mod_length = length % max_length) > 0)count += 1;

                                            while (tmp_c < count){
                                                if (tmp_c + 1 == count){
                                                    tmpBytes = Arrays.copyOfRange(in_bytes,tmp_c * max_length,tmp_c * max_length + mod_length);
                                                }else
                                                    tmpBytes = Arrays.copyOfRange(in_bytes,tmp_c * max_length,tmp_c * max_length + max_length);

                                                ret_c += connection.bulkTransfer(usbOutEndpoint,tmpBytes,tmpBytes.length, 30000);
                                                tmp_c++;
                                            }

                                        }
                                        Logger.d("ret_c:%d,bytes.length:%d",ret_c,length);
                                    }
                                } finally {
                                    connection.releaseInterface(usbInterface);
                                    connection.close();
                                }
                            }else{
                                context.runOnUiThread(()->MyDialog.ToastMessage("独占访问打印机错误！",context,null));
                            }
                        }else{
                            context.runOnUiThread(()->MyDialog.ToastMessage("打开打印机连接错误！",context,null));
                        }
                    }else{
                        context.runOnUiThread(()->MyDialog.ToastMessage("未找到USB输出端口！",context,null));
                    }
                }else{
                    context.runOnUiThread(()->MyDialog.ToastMessage("未找到打印机设备！",context,null));
                }
            }
        });
    }
}
