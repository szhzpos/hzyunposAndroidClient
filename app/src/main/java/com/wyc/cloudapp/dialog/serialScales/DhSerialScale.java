package com.wyc.cloudapp.dialog.serialScales;

import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android_serialport_api.SerialPort;

public class DhSerialScale extends AbstractWeightedScaleImp {
    public DhSerialScale(final String port){
        mPort = port;
    }
    @Override
    public void startRead() {
        CustomApplication.submit(()->{
            try {
                // 打开/dev/ttyUSB0路径设备的串口
                synchronized(this){
                    mSerialPort = new SerialPort(new File(mPort), 9600, 0);
                }
                final InputStream inputStream = mSerialPort.getInputStream();
                final StringBuilder builder = new StringBuilder();
                int size = -1;
                char b;
                double value = 0.0,tmp_v = 0.0;
                while (mReading){
                    size = inputStream.read();
                    if (size != -1){
                        b = (char)(size & 0xFF);
                        switch (b){
                            case 0x0A:
                                if (builder.length() == 5){
                                    tmp_v  = Double.valueOf(builder.toString()) / 1000;
                                    if (!Utils.equalDouble(value,tmp_v)){
                                        value = tmp_v;
                                        if (mOnReadStatus != null)mOnReadStatus.onFinish(0,value);
                                    }
                                }
                                break;
                            case 0x0D:
                                builder.delete(0,builder.length());
                                break;
                            case 0x20:
                                builder.append('0');
                                break;
                            default:
                                builder.append(b);
                                break;
                        }
                    }
                }
            } catch (IOException | SecurityException | NumberFormatException e) {
                e.printStackTrace();
                if (mReading && mOnReadStatus != null)mOnReadStatus.onError(e.getMessage());//读过程中发生错误
            }finally {
                close();
            }
        });
     }

    @Override
    public void stopRead() {
        mReading = false;
        close();
    }
}
