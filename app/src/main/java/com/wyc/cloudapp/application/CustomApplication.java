package com.wyc.cloudapp.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.squareup.leakcanary.LeakCanary;
import com.wyc.cloudapp.BuildConfig;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.DiskLogAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
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

    private JSONObject mCashierInfo,mStoreInfo;
    private String mAppId, mAppSecret,mUrl;

    private long OfflineTime = 0;//离线时间戳

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
        setupLeakCanary();

        Logger.addLogAdapter(new AndroidLogAdapter());
        Logger.addLogAdapter(new DiskLogAdapter());//日志记录磁盘
        CrashHandler.getInstance().init(this);
        registerActivityLifecycleCallbacks(callbacks);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setupLeakCanary() {
        enabledStrictMode();
        LeakCanary.install(this);
    }

    public static void enabledStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
    }

    public boolean initCashierInfoAndStoreInfo(final Context context){
        final JSONObject cas_info = mCashierInfo = new JSONObject();
        final JSONObject st_info = mStoreInfo = getConnParam();
        if (SQLiteHelper.getLocalParameter("cashierInfo",cas_info)){
            if(SQLiteHelper.execSql(cas_info,"SELECT ifnull(pt_user_cname,'') pt_user_cname,ifnull(pt_user_id,'') pt_user_id FROM cashier_info where cas_id = " + cas_info.getString("cas_id"))){
                try {
                    mUrl = st_info.getString("server_url");
                    mAppId = st_info.getString("appId");
                    mAppSecret = st_info.getString("appSecret");
                    mStoreInfo = JSON.parseObject(st_info.getString("storeInfo"));
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "初始化仓库信息错误：" + e.getMessage(),Toast.LENGTH_LONG).show();
                    return false;
                }
            }else {
                Toast.makeText(context, "初始化收银员信息错误：" + cas_info.getString("info"),Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            Toast.makeText(context, "初始化收银员信息错误：" + cas_info.getString("info"),Toast.LENGTH_LONG).show();
            return false;
        }
    }
    public static JSONObject getConnParam(){
        final SharedPreferences preferences= mApplication.getSharedPreferences("conn_param", Context.MODE_PRIVATE);
        return JSON.parseObject(preferences.getString("param","{}"));
    }

    public static void setConnParam(final JSONObject jsonObject){
        //
        final SharedPreferences preferences= mApplication.getSharedPreferences("conn_param", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor= preferences.edit();
        editor.putString("param",jsonObject.toString());
        editor.apply();
    }

    public boolean isNotLogin(){
        return mStoreInfo == null || mCashierInfo == null;
    }

    public JSONObject getCashierInfo(){
        return mCashierInfo;
    }
    public JSONObject getStoreInfo(){
        return mStoreInfo;
    }
    public String getCashierName(){
       return mCashierInfo.getString("cas_name");
    }
    public String getPosNum(){return Utils.getNullStringAsEmpty(mCashierInfo,"pos_num");}
    public String getCashierId(){
        return mCashierInfo.getString("cas_id");
    }
    public String getCashierCode(){
        return Utils.getNullStringAsEmpty(mCashierInfo,"cas_code");
    }
    public String getPtUserId(){
        return Utils.getNullStringAsEmpty(mCashierInfo,"pt_user_id");
    }
    public String getStoreName(){
        return mStoreInfo.getString("stores_name");
    }
    public String getStoreId(){
        return Utils.getNullStringAsEmpty(mStoreInfo,"stores_id");
    }
    public String getWhId(){
        return Utils.getNullStringAsEmpty(mStoreInfo,"wh_id");
    }
    public String getStoreIdWithSharedPreferences(){
        final JSONObject conn_param = getConnParam();
        if (conn_param.isEmpty()){
            return "";
        }
        return Utils.getNullOrEmptyStringAsDefault(JSON.parseObject(conn_param.getString("storeInfo")),"stores_id","");
    }

    public String getAppId(){
        return mAppId;
    }
    public String getAppSecret(){
        return mAppSecret;
    }
    public String getUrl(){
        return mUrl;
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

    public void exit(){
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

    public void initSyncManagement(final String url, final String appid, final String appsecret, final String stores_id, final String pos_num, final String operid){
        mSyncManagement.initSync(url,appid,appsecret,stores_id,pos_num,operid);
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
        mSyncManagement.sync_retail_order(false);
    }

    public void reuplaod_retail_order(){
        mSyncManagement.sync_retail_order(true);
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

    public void clearBasicsData(){
        final List<String> names = mSyncManagement.getDataTableName();
        final StringBuilder err = new StringBuilder();
        for (String name : names){
            if (SQLiteHelper.execDelete(name,null,null,err) < 0){
                Toast.makeText(this,err,Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void runInMainThread(Runnable r){
        if (!mApplication.myhandler.hasCallbacks(r)) mApplication.myhandler.post(r);
    }

    public static void sendMessage(int what){
        mApplication.myhandler.obtainMessage(what).sendToTarget();
    }
    public static void sendMessage(int what,  Object obj){
        mApplication.myhandler.obtainMessage(what,obj).sendToTarget();
    }
    public static void sendMessage(int what,int arg1,int arg2){
        mApplication.myhandler.obtainMessage(what,arg1,arg2).sendToTarget();
    }
    public static void sendMessageAtFrontOfQueue(int what){
        mApplication.myhandler.sendMessageAtFrontOfQueue(mApplication.myhandler.obtainMessage(what));
    }

    public static void postAtFrontOfQueue(Runnable r){
        mApplication.myhandler.postAtFrontOfQueue(r);
    }

    public static void postDelayed(Runnable r, long delayMillis){
        if (!mApplication.myhandler.hasCallbacks(r))mApplication.myhandler.postDelayed(r,delayMillis);
    }
    public static void removeCallbacks(Runnable r){
        mApplication.myhandler.removeCallbacks(r);
    }

    private static class Myhandler extends Handler {
        private final CustomApplication app;
        private Myhandler(Looper looper, final CustomApplication application){
            super(looper);
            app = application;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MessageID.NETWORKSTATUS_ID:
                    if ((msg.obj instanceof Boolean)){
                        boolean code = (boolean) msg.obj;
                        if (app.getAndSetNetworkStatus(code) != code && app.mCallback != null){
                            app.mCallback.handleMessage(this,msg);
                        }
                    }
                    break;
                case MessageID.TRANSFERSTATUS_ID:
                    if ((msg.obj instanceof Boolean)){
                        boolean code = (boolean) msg.obj;

                        if (app.getAndSetTransferStatus(code) != code){
                            if (code){
                                msg.arg1 = 2;
                            }else
                                msg.arg1 = 3;
                        }else
                            msg.arg1 = 1;

                        if (app.mCallback != null)app.mCallback.handleMessage(this,msg);
                    }
                    break;
                case MessageID.START_SYNC_ORDER_INFO_ID:
                    Toast.makeText(app,"开始上传数据",Toast.LENGTH_SHORT).show();
                    break;
                case MessageID.FINISH_SYNC_ORDER_INFO_ID:
                    Toast.makeText(app,"数据上传完成",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    if (app.mCallback != null)app.mCallback.handleMessage(this,msg);
            }
        }
    }
    public interface MessageCallback{
        void handleMessage(final Handler handler,final Message msg);
    }

    private boolean getAndSetTransferStatus(boolean b){
        return mTransferStatus.getAndSet(b);
    }
    private boolean getAndSetNetworkStatus(boolean b){
        return mNetworkStatus.getAndSet(b);
    }

    public static void initDb(final String stores_id){
        if (!mApplication.getStoreId().equals(stores_id)){
            //输入的门店ID和当前的不一样需要重新初始化数据库，因为数据库名称和门店ID相关。
            SQLiteHelper.closeDB();
        }
        SQLiteHelper.initDb(mApplication,stores_id);
    }

    public static String getGoodsImgSavePath(){
        return getSaveDirectory("hzYunPos/goods_img");
    }

    public static String getCrashSavePath(){
        return getSaveDirectory("hzYunPos/crash_log");
    }

    /*
    * 创建数据目录路，如果外部存储不存在或者在外部存储创建失败则由 Context.getDir 创建并返回;需要注意如果不在外部存储创建，App卸载时将被删除。
    * @param str 目录名称
    * @return 返回目录全路径字符串
    * */
    @NonNull
    private static String getSaveDirectory(String str) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + str + "/";
            final File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return mApplication.getDir(str, Context.MODE_PRIVATE).getAbsolutePath();
                }
            }
            return rootDir;
        } else {
            return mApplication.getDir(str, Context.MODE_PRIVATE).getAbsolutePath();
        }
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
                    final Future<?> future = (Future<?>) r;
                    if (future.isDone())future.get();
                } catch (ExecutionException | CancellationException | InterruptedException ee) {
                    t = ee.getCause();
                }
            }
            if (t != null){
                final Handler handler = mApplication.myhandler;
                final String sz = String.format(Locale.CHINA,"%s throw exception:%s",this.getClass().getSimpleName(),t.getMessage());
                Logger.w("%s%s",sz, Utils.formatStackTrace(t.getStackTrace()));
                handler.post(()->{
                    if (mApplication.mActivities.isEmpty()){
                        Toast.makeText(mApplication,sz,Toast.LENGTH_LONG).show();
                    }else
                        MyDialog.showErrorMessageToModalDialog(mApplication.mActivities.lastElement(),sz);
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
                int max_offline_hours = 72;
                if (offline_time - old_offline_time > max_offline_hours * 3600 * 1000){
                    final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    final String sz_offline_time = String.format(Locale.CHINA,"离线时间已超过%d小时;最后离线时间:%s,当前时间:%s",max_offline_hours,sf.format(new Date(old_offline_time)),sf.format(new Date()));
                    MyDialog.displayErrorMessage(context,sz_offline_time);
                    code = false;
                }
            }
        }else {
            Toast.makeText(context,object.getString("info"),Toast.LENGTH_LONG).show();
        }
        return code;
    }

    public static void updateOfflineTime(long timestamp){
        if (mApplication.OfflineTime != timestamp){
            mApplication.OfflineTime = timestamp;

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
                    MyDialog.ToastMessageInMainThread(object.getString("info"));
                }
            }
            if (null != sql ){
                if (!SQLiteHelper.execSql(object,sql)){
                    final String err = "更新离线时间错误:" + object.getString("v");
                    Logger.e(err);
                    MyDialog.ToastMessageInMainThread(err);
                }
            }
        }
    }
    private static void saveOfflineTime(final Context context,final JSONObject object){
        final StringBuilder err = new StringBuilder();
        mApplication.OfflineTime = object.getLongValue("v");
        if (!SQLiteHelper.saveLocalParameter("offline_time",object,"前台离线时间",err)){
            err.insert(0,"保存离线时间错误:");
            Logger.e(err.toString());
            Toast.makeText(context,err.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
