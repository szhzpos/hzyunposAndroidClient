package com.wyc.cloudapp.application;

import android.Manifest;
import android.app.Application;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.broadcast.NetChangeMonitor;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.DiskLogAdapter;
import com.wyc.cloudapp.logger.LogcatLogStrategy;
import com.wyc.cloudapp.logger.Logger;

import java.io.File;

/**
 * Created by Administrator on 2018-04-17.
 */

public class CustomApplication extends Application {
    private NetChangeMonitor netChangeMonitor;
    private volatile int netState = 1,netState_mobile = 1;//WiFi 连接状态 1 连接 0 其他
    public CustomApplication(){
        super();
    }
    @Override
    public  void  onCreate(){
        super.onCreate();
        Logger.addLogAdapter(new AndroidLogAdapter());
        Logger.addLogAdapter(new DiskLogAdapter());//日志记录磁盘
        netChangeMonitor = new NetChangeMonitor();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("confirm_connection");
        registerReceiver(netChangeMonitor,intentFilter);
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        SQLiteHelper.closeDB();
        if (netChangeMonitor != null){
            unregisterReceiver(netChangeMonitor);
        }
    }
    public synchronized int getNetState(){
        return netState|netState_mobile;
    }
    public synchronized int getNetState_mobile(){
        return netState_mobile;
    }

    public synchronized void setNetState_mobile(int state){
        netState_mobile = state;
    }
    public synchronized void setNetState(int state){
        netState = state;
    }

}
