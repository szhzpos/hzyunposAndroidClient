package com.wyc.cloudapp.dialog.serialScales;

import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android_serialport_api.SerialPort;

public class DjSerialScale extends AbstractSerialScale {
    private Future<?> mTask;
    public DjSerialScale(final String port){
        mPort = port;
    }
    @Override
    public void startRead() {
        mTask = CustomApplication.submit(()->{
            try {
                synchronized(this){
                    mSerialPort = new SerialPort(new File(mPort), 9600, 0);
                }
                final InputStream inputStream = mSerialPort.getInputStream();
                final OutputStream outputStream = mSerialPort.getOutputStream();
                final StringBuilder stringBuilder = new StringBuilder();
                int size = -1;
                char b;
                double value = 0.0,tmp_v = 0.0;
                while (mReading){
                    size = inputStream.read();
                    if (size != -1){
                        b = (char)(size & 0x7F);
                        switch (b){
                            case 0x02:
                                stringBuilder.delete(0,stringBuilder.length());
                                break;
                            case 0x03:
                                tmp_v  = Double.valueOf(stringBuilder.substring(stringBuilder.indexOf(".") - 1,stringBuilder.indexOf("k")));
                                if (!Utils.equalDouble(value,tmp_v)){
                                    value = tmp_v;
                                    if (mOnReadStatus != null)mOnReadStatus.onFinish(value);
                                }
                                break;
                            default:
                                stringBuilder.append(b);
                                break;
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
                if (mReading)if (mOnReadStatus != null)mOnReadStatus.onError(e.getMessage());//读过程中发生错误
            }finally {
                close();
            }
        });
    }

    @Override
    public void stopRead() {
        mReading = false;
        try {
            mTask.get(300, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException | CancellationException e) {
            e.printStackTrace();
            if (e instanceof TimeoutException ){//如果超时重新关闭
                close();
            }
        }
    }
    @Override
    public synchronized void close(){
        if (mSerialPort != null){
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}
