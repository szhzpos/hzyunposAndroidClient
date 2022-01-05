package com.wyc.cloudapp.print;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.bean.PrinterStatus;
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.print.receipts.IReceipts;
import com.wyc.cloudapp.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.WINDOW_SERVICE;

public final class Printer {
    private static ImageView mICO;
    public static final String  CHARACTER_SET = "GB2312";
    public static final String REPLACEMENT = "***";
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
    public static final byte[] LINE_SPACING_10 = {0x1b,0x33,0x10};
    public static final byte[] LINE_SPACING_16 = {0x1b,0x33,0x16};
    public static final byte[] LINE_SPACING_48 = {0x1b,0x33,0x48};
    public static final byte[] LINE_SPACING_4 = {0x1b,0x33,0x04};
    public static final byte[] LINE_SPACING_2 = {0x1b,0x33,0x02};
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

    /**
     * 切纸
     */
    public static final byte[] CUT  = {0x1B, 0x6D};

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
        int middle = LINE_BYTE_SIZE - leftTextLength - rightTextLength;
        if (align == 1)
            for (int i = 0; i < middle; i++) {
                sb.append(" ");
            }
            else
            sb.append(" ");

        sb.append(rightText);
        return sb.toString();
    }
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }
    public static String printThreeData(int space,String leftText, String middleText, String rightText) {
        StringBuilder sb = new StringBuilder();
        int max_left_len = LEFT_LENGTH;
        if (isContainChinese(leftText)){
            // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
            max_left_len = LEFT_TEXT_MAX_LENGTH;
        }
        if (leftText.length() > max_left_len) {
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
        final int mid_right_margin = 10 - space,left_margin = 14;
        int left_byte_size = getBytesLength(leftText);
        if (left_byte_size > left_margin) {
            leftText = leftText.substring(0, left_margin / 2) + "..";
            sb.append(leftText).append(spacing);
        }else {
            sb.append(leftText);
            for (int i = 0,k = left_margin - left_byte_size; i < k ; i++) {
                sb.append(spacing);
            }
        }

        // 计算左侧文字和中间文字的空格长度
        int mid_byte_size = getBytesLength(middleText);
        if (mid_byte_size < mid_right_margin) {
            for (int i = 0; i < mid_right_margin - mid_byte_size; i++) {
                sb.append(spacing);
            }
            sb.append(middleText);
        }else {
            sb.append(substringWithChar(middleText,0,mid_right_margin));
        }

        int rig_byte_size = getBytesLength(rightText);
        if (rig_byte_size < mid_right_margin) {
            for (int i = 0; i < mid_right_margin - rig_byte_size; i++) {
                sb.append(spacing);
            }
            sb.append(rightText);
        }else {
            sb.append(substringWithChar(rightText,0,mid_right_margin));
        }

        return sb.toString();
    }
    private static String substringWithChar(String sz,int s,int e){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0,size = sz.length();i < size;i++){
            if (i >= s && i<= e){
                stringBuilder.append(sz.charAt(i));
            }
        }
        return stringBuilder.toString();
    }

    public static String commandToStr(byte[] bytes){
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    public static boolean getPrinterSetting(@NonNull final JSONObject object){
        return SQLiteHelper.getLocalParameter("printer",object);
    }

    public static void printByBluetooth(final String c,final String device_addr){
        if (Utils.isNotEmpty(c)){
            try {
                bluetooth_print(c.getBytes(CHARACTER_SET),device_addr);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private static void bluetooth_print(final byte[] content, final String device_addr){
        if(content != null && Utils.isNotEmpty(device_addr)){
            MyDialog.toastMessage(CustomApplication.self().getString(R.string.begin_print));
            CustomApplication.execute(()->{
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null){
                    if (bluetoothAdapter.isEnabled()){
                        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device_addr);
                        synchronized (Printer.class){
                            try (BluetoothSocket bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                 BufferedOutputStream outputStream = new BufferedOutputStream(bluetoothSocket.getOutputStream());){

                                bluetoothSocket.connect();

                                //outputStream.write(RESET);

                                byte[] tmpBytes;
                                int length = content.length,max_length = 128;
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
                                        outputStream.flush();

                                        LockSupport.parkUntil(50);

                                        tmp_c++;
                                    }
                                }
                                MyDialog.toastMessage(CustomApplication.self().getString(R.string.end_print));
                                Logger.d("count:%d,bytes.length:%d",count,length);
                            } catch (IOException e) {
                                e.printStackTrace();
                                CustomApplication.postAtFrontOfQueue(()->MyDialog.ToastMessage("打印错误：" + e.getMessage(), null));
                            }
                        }
                    }else{
                        CustomApplication.postAtFrontOfQueue(()->MyDialog.ToastMessage("蓝牙已关闭！", null));
                    }
                }
            });
        }
    }

    public static void updatePrintIcon(final MainActivity activity,final int x,final int y){
        if (mICO != null){
            final WindowManager wm = (WindowManager)activity.getSystemService(WINDOW_SERVICE);
            final WindowManager.LayoutParams wLayout = (WindowManager.LayoutParams) mICO.getLayoutParams();
            wLayout.x = x;
            wLayout.y = (int) (y - activity.getStatusBarHeight() +  CustomApplication.getDimension(R.dimen.size_8));
            wm.updateViewLayout(mICO,wLayout);
        }
    }

    public static void dismissPrintIcon(final MainActivity activity){
        if (mICO != null){
            final WindowManager wm = (WindowManager)activity.getSystemService(WINDOW_SERVICE);
            if (wm != null)wm.removeViewImmediate(mICO);
            mICO = null;
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    public static void showPrintIcon(final MainActivity activity){
        if (!Settings.canDrawOverlays(activity))return;
        final WindowManager wm = (WindowManager)activity.getSystemService(WINDOW_SERVICE);
        if (wm != null){

            final WindowManager.LayoutParams wLayout = new WindowManager.LayoutParams();
            wLayout.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
            wLayout.format= PixelFormat.RGBA_8888;
            wLayout.gravity= Gravity.LEFT|Gravity.TOP;
            wLayout.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;;
            wLayout.height = (int) CustomApplication.getDimension(R.dimen.size_32);
            wLayout.width = (int) CustomApplication.getDimension(R.dimen.size_32);

            if (mICO != null)wm.removeViewImmediate(mICO);
            mICO = new ImageView(activity);
            showIco(activity);

            mICO.setBackgroundColor(activity.getColor(R.color.lightBlue));

            final Display display = wm.getDefaultDisplay();
            final Point point = new Point();
            display.getSize(point);
            wLayout.x = point.x - 128;
            wLayout.y = point.y / 2;
            wm.addView(mICO,wLayout);

            mICO.setOnTouchListener(new View.OnTouchListener() {
                private double touchX,touchY;
                private boolean mIsMove;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            touchX = event.getRawX() - wLayout.x;
                            touchY = event.getRawY() - wLayout.y;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            final float raw_x = event.getRawX(),raw_y = event.getRawY();
                            if (touchX != raw_x - wLayout.x || touchY != raw_y - wLayout.y){//部分手机点击会触发MotionEvent.ACTION_MOVE事件，但是位置没发生改变
                                mIsMove = true;
                                wLayout.x = (int) (raw_x - touchX);
                                wLayout.y = (int) (raw_y - touchY);
                                wm.updateViewLayout(mICO,wLayout);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (!mIsMove){
                                activity.switchPrintStatus();
                                showIco(activity);
                            }else
                                mIsMove = false;
                            break;
                    }
                    return false;
                }
            });
        }
    }
    private static void showIco(final MainActivity context){
        final Bitmap printer = BitmapFactory.decodeResource(context.getResources(),R.drawable.printer);
        if (null != mICO && null != printer){
            final PrinterStatus status = PrinterStatus.getPrinterStatus();
            if (status.isOpen()){
                mICO.setImageBitmap(printer);
            }else if (status.isClose()){
                mICO.setImageBitmap(drawPrintClose(printer));
            }else mICO.setImageBitmap(drawPrintWarn(printer));
        }
    }
    public static Bitmap drawPrintClose(Bitmap printer){
        if (printer == null)printer = BitmapFactory.decodeResource(CustomApplication.self().getResources(),R.drawable.printer);
        if (printer == null)return null;
        return PrintUtilsToBitbmp.drawErrorToBitmap(printer, (int) CustomApplication.getDimension(R.dimen.size_15),(int) CustomApplication.getDimension(R.dimen.size_15));
    }
    public static Bitmap drawPrintWarn(Bitmap printer){
        if (printer == null)printer = BitmapFactory.decodeResource(CustomApplication.self().getResources(),R.drawable.printer);
        if (printer == null)return null;
        return PrintUtilsToBitbmp.drawWarnToBitmap(printer);
    }
}
