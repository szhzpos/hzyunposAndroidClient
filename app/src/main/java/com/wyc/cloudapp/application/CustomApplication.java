package com.wyc.cloudapp.application;

import android.app.Application;
import android.content.IntentFilter;

import com.wyc.cloudapp.broadcast.NetChangeMonitor;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.Logger;

/**
 * Created by Administrator on 2018-04-17.
 */

public class CustomApplication extends Application {
    private SQLiteHelper sqLiteHelper;
    private NetChangeMonitor netChangeMonitor;
    private volatile int netState = 1,netState_mobile = 1;//WiFi 连接状态 1 连接 0 其他
    public CustomApplication(){
        super();
    }
    @Override
    public  void  onCreate(){
        super.onCreate();
        Logger.addLogAdapter(new AndroidLogAdapter());
        sqLiteHelper = new SQLiteHelper(this);
        netChangeMonitor = new NetChangeMonitor();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("confirm_connection");
        registerReceiver(netChangeMonitor,intentFilter);
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        if (sqLiteHelper != null){
            sqLiteHelper.closeDB();
        }
        if (netChangeMonitor != null){
            unregisterReceiver(netChangeMonitor);
        }
    }

    public SQLiteHelper getSqLiteHelper(){
        return sqLiteHelper;
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
