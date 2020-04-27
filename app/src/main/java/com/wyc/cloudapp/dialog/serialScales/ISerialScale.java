package com.wyc.cloudapp.dialog.serialScales;

import java.io.IOException;

import android_serialport_api.SerialPort;

public interface ISerialScale {
    void startRead();
    void stopRead();
    void close();
}
