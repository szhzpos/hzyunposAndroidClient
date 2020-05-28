package com.wyc.cloudapp.application;

import android.app.Application;
import android.content.IntentFilter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.wyc.cloudapp.broadcast.GlobalBroadcast;

import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.DiskLogAdapter;
import com.wyc.cloudapp.logger.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomApplication extends Application {
    //使用无界限阻塞队列，不会触发拒绝策略；线程数最大等于核心线程数，如果所有线程都在运行则任务会进入队列等待执行<可能引发内存问题>，
    private static final ScheduledThreadPoolExecutor THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2 + 1);
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

    static {
        //是否输出值为null的字段,默认为false
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        //数值字段如果为null,输出为0,而非null
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteNullNumberAsZero.getMask();
        //List字段如果为null,输出为[],而非null
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteNullListAsEmpty.getMask();
        //字符类型字段如果为null,输出为 "",而非null
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteNullStringAsEmpty.getMask();
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

    public static Future<?> submit(Runnable task){
        return THREAD_POOL_EXECUTOR.submit(task);
    }

    public static <T> Future<T> submit(Callable<T> task){
        return THREAD_POOL_EXECUTOR.submit(task);
    }

    public static boolean removeTask(Runnable task){
        return THREAD_POOL_EXECUTOR.remove(task);
    }
    public static long getTaskCount(){
        return THREAD_POOL_EXECUTOR.getActiveCount();
    }
}
