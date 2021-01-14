package com.wyc.cloudapp.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.DiskLogAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CustomApplication extends Application {
    //使用无界限阻塞队列，不会触发拒绝策略；线程数最大等于核心线程数，如果所有线程都在运行则任务会进入队列等待执行<可能引发内存问题>，
    private static final DefaultScheduledThreadPoolExecutor THREAD_POOL_EXECUTOR = new DefaultScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);
    private static CustomApplication mApplication;
    private final Vector<Activity> mActivities;

    private final SyncManagement mSyncManagement;
    private final AtomicBoolean mTransferStatus;
    private final AtomicBoolean mNetworkStatus;
    private MessageCallback mCallback;
    private final Myhandler myhandler;
    public CustomApplication(){
        super();
        mApplication = this;
        mActivities = new Vector<>();
        myhandler  = new Myhandler(Looper.myLooper(),this);
        mNetworkStatus = new AtomicBoolean(true);
        mTransferStatus = new AtomicBoolean(true);//传输状态
        mSyncManagement = new SyncManagement();
    }
    @Override
    public  void  onCreate(){
        super.onCreate();
        Logger.addLogAdapter(new AndroidLogAdapter());
        Logger.addLogAdapter(new DiskLogAdapter());//日志记录磁盘

        registerActivityLifecycleCallbacks(callbacks);
    }

    private final ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            mActivities.add(activity);
        }
        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }
        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }
        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }
        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }
        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            mActivities.remove(activity);
            if (mActivities.isEmpty()){
                THREAD_POOL_EXECUTOR.shutdownNow();
                Logger.d("THREAD_POOL_EXECUTOR shutdowned");
                exit();
            }
        }
    };

    private void exit(){
        myhandler.removeCallbacksAndMessages(null);
        mSyncManagement.quit();
        mApplication = null;
        SQLiteHelper.closeDB();
        android.os.Process.killProcess(android.os.Process.myPid());
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

    public synchronized void setNetState_mobile(int state){
        //WiFi 连接状态 1 连接 0 其他
    }
    public synchronized void setNetState(int state){
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

    public static CustomApplication self(){
        return mApplication;
    }

    public void setNetworkStatus(boolean b){
        mNetworkStatus.set(b);
    }
    public boolean isConnection(){
        return mNetworkStatus.get();
    }
    public void registerHandleMessage(final MessageCallback callback){
        mCallback = callback;
    }

    public boolean getAndSetTransferStatus(boolean b){
        return mTransferStatus.getAndSet(b);
    }
    public boolean getAndSetNetworkStatus(boolean b){
        return mNetworkStatus.getAndSet(b);
    }

    public void initSyncManagement(final String url, final String appid, final String appsecret, final String stores_id, final String pos_num, final String operid){
        mSyncManagement.initSync(url,appid,appsecret,stores_id,pos_num,operid);
    }

    public Handler getAppHandler(){
        return myhandler;
    }

    public void data_upload(){
        mSyncManagement.sync_order_info();
    }

    public void sync_order_info(){
        mSyncManagement.sync_order_info();
    }

    public void sync_transfer_order(){
        mSyncManagement.sync_transfer_order();
    }

    public void sync_retail_order(){
        mSyncManagement.sync_retail_order();
    }

    public void start_sync(boolean b){
        mSyncManagement.start_sync(b);
    }

    public void pauseSync(){
        mSyncManagement.pauseSync();
    }

    public void continueSync(){
        mSyncManagement.continueSync();
    }

    public void stop_sync(){
        mSyncManagement.stop_sync();
    }

    public void manualSync(){
        mSyncManagement.afresh_sync();
    }
    public void sync_refund_order(){
        if (mSyncManagement != null) mSyncManagement.sync_refund_order();
    }

    public void resetSync(){
        mSyncManagement.rest();
    }


    private static class Myhandler extends Handler {
        private final CustomApplication app;
        private Myhandler(Looper looper, final CustomApplication application){
            super(looper);
            app = application;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == MessageID.NETWORKSTATUS_ID && (msg.obj instanceof Boolean)){//统一更新离线时间
                boolean code = (boolean) msg.obj;
                if (code){
                    updateOfflineTime(-1);
                }else {
                    updateOfflineTime(System.currentTimeMillis());
                }
            }
            if (app.mCallback != null)app.mCallback.handleMessage(this,msg);
        }
    }
    public interface MessageCallback{
        void handleMessage(final Handler handler,final Message msg);
    }

    private static class DefaultScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor{

        public DefaultScheduledThreadPoolExecutor(int corePoolSize) {
            super(corePoolSize);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (t == null && r instanceof Future<?>) {
                try {
                    ((Future<?>) r).get();
                } catch (ExecutionException | InterruptedException ee) {
                    t = ee.getCause();
                }
            }
            if (t != null){
                final Handler handler = mApplication.getAppHandler();
                final String sz = String.format(Locale.CHINA,"%s throw exception:%s",this.getClass().getSimpleName(),t.getMessage());
                Logger.w("%s%s",sz, Utils.formatStackTrace(t.getStackTrace()));
                handler.post(()->{
                    if (mApplication.mActivities.isEmpty()){
                        handler.post(()-> Toast.makeText(mApplication,sz,Toast.LENGTH_LONG).show());
                    }else
                        MyDialog.showErrorMessageToModalDialog(mApplication.mActivities.get(0),sz);
                });
            }
        }
    }

    public static boolean verifyOfflineTime(final Context context){
        long offline_time = System.currentTimeMillis();
        final JSONObject object = new JSONObject();
        boolean code = SQLiteHelper.getLocalParameter("offline_time",object);
        if (code){
            if (object.isEmpty()){
                object.put("v",offline_time);
                saveOfflineTime(context,object);
                code = true;
            }else {
                long old_offline_time = Utils.getNotKeyAsNumberDefault(object,"v",Long.valueOf(offline_time));
                if (offline_time - old_offline_time > 72 * 3600 * 1000){
                    final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    final String sz_offline_time = String.format(Locale.CHINA,"最后离线时间:%s,当前时间:%s",sf.format(new Date(old_offline_time)),sf.format(new Date()));
                    MyDialog.displayErrorMessage(context,sz_offline_time);
                    code = false;
                }
            }
        }else {
            Toast.makeText(context,object.getString("info"),Toast.LENGTH_LONG).show();
        }
        return code;
    }

    private static void updateOfflineTime(long timestamp){
        String sql = null;
        final JSONObject object = new JSONObject();
        if (timestamp <= 0){
            sql = "delete from local_parameter where parameter_id = 'offline_time'";
        }else {
            boolean code = SQLiteHelper.getLocalParameter("offline_time",object);
            if (code){
                if (object.isEmpty()){
                    object.put("v",timestamp);
                    saveOfflineTime(mApplication,object);
                }else {
                    object.put("v",timestamp);
                    sql = "update local_parameter set parameter_content = '"+ object +"' where parameter_id = 'offline_time'";
                }
            }else {
                Toast.makeText(mApplication,object.getString("info"),Toast.LENGTH_LONG).show();
            }
        }
        if (null != sql ){
            if (!SQLiteHelper.execSql(object,sql)){
                final String err = "更新离线时间错误:" + object.getString("v");
                Logger.e(err);
                Toast.makeText(mApplication,err,Toast.LENGTH_LONG).show();
            }
        }
    }
    private static void saveOfflineTime(final Context context,final JSONObject object){
        final StringBuilder err = new StringBuilder();
        if (!SQLiteHelper.saveLocalParameter("offline_time",object,"前台离线时间",err)){
            err.insert(0,"保存离线时间错误:");
            Logger.e(err.toString());
            Toast.makeText(context,err.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
