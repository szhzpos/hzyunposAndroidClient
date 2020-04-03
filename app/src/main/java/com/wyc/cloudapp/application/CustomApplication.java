package com.wyc.cloudapp.application;

import android.app.Application;
import android.content.IntentFilter;

import com.wyc.cloudapp.broadcast.GlobalBroadcast;

import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.DiskLogAdapter;
import com.wyc.cloudapp.logger.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomApplication extends Application {
    private static final ScheduledThreadPoolExecutor THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2 + 2);
    private volatile int netState = 1,netState_mobile = 1;//WiFi 连接状态 1 连接 0 其他
    public CustomApplication(){
        super();
    }

    @Override
    public  void  onCreate(){
        super.onCreate();
        Logger.addLogAdapter(new AndroidLogAdapter());
        Logger.addLogAdapter(new DiskLogAdapter());//日志记录磁盘
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("confirm_connection");
        registerReceiver(new GlobalBroadcast(),intentFilter);
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

    public static void execute(Runnable runnable){
        THREAD_POOL_EXECUTOR.execute(runnable);
    }
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit){
        return THREAD_POOL_EXECUTOR.scheduleAtFixedRate(command,initialDelay,period,unit);
    }
    public static boolean removeTask(Runnable task){
        return THREAD_POOL_EXECUTOR.remove(task);
    }
    public static long getTaskCount(){
        return THREAD_POOL_EXECUTOR.getActiveCount();
    }
}
