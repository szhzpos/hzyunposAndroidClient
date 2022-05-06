package com.wyc.cloudapp.customerView.bean;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.customerView.ICustomerView;
import com.wyc.cloudapp.dialog.MyDialog;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import android_serialport_api.SerialPort;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.customerView.bean
 * @ClassName: Serial_CV
 * @Description: 串口顾显
 * @Author: wyc
 * @CreateDate: 2022/5/6 9:39
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/5/6 9:39
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class Serial_CV implements ICustomerView {
    private volatile SerialPort mSerialPort = null;
    private final Object mLock = new Object();
    public Serial_CV(final String port,Integer rate){
        try {
            mSerialPort = new SerialPort(new File(port), rate, 0);
        } catch (IOException e) {
            e.printStackTrace();
            MyDialog.toastMessage(R.string.init_cv_error);
        }
    }

    private byte[] doubleToBytes(double c){
        final String sz = String.format(Locale.CHINA,"%.2f",c);
        return sz.getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public void writPrice(double price) {
        write(new byte[]{0x0C});
        write(new byte[]{0x1B,0x73,0x31});
        writeContent(price);
    }

    @Override
    public void writRealPay(double amt) {
        write(new byte[]{0x0C});
        write(new byte[]{0x1B,0x73,0x32});
        writeContent(amt);
    }

    @Override
    public void writChange(double amt) {
        write(new byte[]{0x0C});
        write(new byte[]{0x1B,0x73,0x34});
        writeContent(amt);
    }

    private void writeContent(double num){
        byte[] c = doubleToBytes(num);
        byte[] command = new byte[4 + c.length];
        command[0] = 0x1B;
        command[1] = 0x51;
        command[2] = 0x41;
        System.arraycopy(c,0,command,3,c.length);
        command[command.length - 1] = 0x0D;
        write(command);
    }

    private void write(byte[] bytes){
        CustomApplication.execute(()->{
            synchronized (mLock){
                if (mSerialPort != null){
                    try {
                        mSerialPort.getOutputStream().write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public  void clear() {
        synchronized(mLock){
            if (mSerialPort != null){
                mSerialPort.close();
                mSerialPort = null;
            }
        }
    }
}
