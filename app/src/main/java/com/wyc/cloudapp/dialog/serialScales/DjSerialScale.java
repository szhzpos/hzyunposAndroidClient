package com.wyc.cloudapp.dialog.serialScales;

import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android_serialport_api.SerialPort;

public class DjSerialScale extends AbstractSerialScaleImp {
    public DjSerialScale(final String port){
        mPort = port;
    }
    @Override
    public void startRead() {
        CustomApplication.submit(()->{
            try {
                synchronized(this){
                    mSerialPort = new SerialPort(new File(mPort), 9600, 0);
                }
                final InputStream inputStream = mSerialPort.getInputStream();
                final StringBuilder stringBuilder = new StringBuilder();
                int size = -1,start,end,stat = OnReadStatus.STABLE,statTmp ;
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
                                statTmp = stringBuilder.indexOf("S") != -1 ? OnReadStatus.STABLE : stringBuilder.indexOf("U") != -1 ? OnReadStatus.NO_STABLE : OnReadStatus.OTHER;

                                start = stringBuilder.indexOf(" ");
                                if (start == -1)start = stringBuilder.indexOf("-");

                                end = stringBuilder.indexOf("k");
                                if ( -1 < start && start < stringBuilder.length() && start <= end && end < stringBuilder.length()){
                                    tmp_v  = Double.parseDouble(stringBuilder.substring(start,end));
                                    if (!Utils.equalDouble(value,tmp_v) || stat != statTmp){
                                        value = tmp_v;
                                        stat = statTmp;
                                        if (mOnReadStatus != null)mOnReadStatus.onFinish(stat,value);
                                    }
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
