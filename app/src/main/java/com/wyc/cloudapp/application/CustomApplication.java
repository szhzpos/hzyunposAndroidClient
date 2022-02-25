package com.wyc.cloudapp.application;

import static com.wyc.cloudapp.constants.MessageID.SYNC_DIS_INFO_ID;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.squareup.leakcanary.LeakCanary;
import com.wyc.cloudapp.BuildConfig;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.bean.ModulePermission;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.DiskLogAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private static volatile Toast mGlobalToast;
    private final Vector<Activity> mActivities;

    private final SyncManagement mSyncManagement;
    private final AtomicBoolean mTransferStatus;
    private final AtomicBoolean mNetworkStatus;
    private final List<MessageCallback> mCallbacks;
    private final MyHandler myhandler;

    private JSONObject mCashierInfo,mStoreInfo;
    private String mAppId, mAppSecret,mUrl;

    private long OfflineTime = 0;//离线时间戳

    private List<ModulePermission> modulePermissions = new ArrayList<>();

    /*
    * 业务模式 true 正常模式 false 练习收银模式
    * 练习收银模式下：1、本地库直接复制正常模式的本地库，所以要求必须至少一次进入正常模式同步基本数据 2、禁用所有业务同步功能 3、所有需要联网的支付方式除了移动支付之外都必须禁用
    * 4、练习收银模式可以通过软件内部切换；正常收银则必须重新登陆软件
     * */
    private boolean mBusinessMode;

    public CustomApplication(){
        super();
        mBusinessMode = true;
        mApplication = this;
        mCallbacks = new ArrayList<>();
        mActivities = new Vector<>();

        myhandler  = new MyHandler(Looper.myLooper());
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
        registerActivityLifecycleCallbacks(activityCallbacks);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        final Resources res = super.getResources();
        final Configuration configuration = res.getConfiguration();
        if (configuration.fontScale != 1f){
            configuration.fontScale = 1f;
            res.updateConfiguration(configuration,res.getDisplayMetrics());
        }
        return res;
    }


    private void setupLeakCanary() {
        enabledStrictMode();
        LeakCanary.install(this);
    }

    private static void enabledStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        }
    }

    private void initModulePermission(){
        modulePermissions = ModulePermission.getModulePermission();
    }

    public @NonNull List<ModulePermission> getModulePermissions(){
        return modulePermissions;
    }

    public static void showGlobalToast(final String message){
        if (Looper.myLooper() == Looper.getMainLooper()){
            showToast(message);
        }else postAtFrontOfQueue(()-> showToast(message));
    }
    private static void showToast(final String message){
        if (mGlobalToast == null){
            mGlobalToast = Toast.makeText(CustomApplication.self(),"",Toast.LENGTH_LONG);
        }
        mGlobalToast.setText(message);
        mGlobalToast.show();
    }

    public static void cancelGlobalToast(){
        if (Looper.myLooper() == Looper.getMainLooper()){
            cancelToast();
        }else postAtFrontOfQueue(CustomApplication::cancelToast);
    }
    private static void cancelToast(){
        if (mGlobalToast != null)mGlobalToast.cancel();
    }

    public static boolean isPracticeMode(){
        return !mApplication.mBusinessMode;
    }
    public static void enterPracticeMode(){
        mApplication.mBusinessMode = false;
    }

    public boolean initCashierInfoAndStoreInfo(@NonNull final StringBuilder err){
        mCashierInfo = new JSONObject();
        if (SQLiteHelper.getLocalParameter("cashierInfo",mCashierInfo) &&
                SQLiteHelper.execSql(mCashierInfo,"SELECT ifnull(pt_user_cname,'') pt_user_cname,ifnull(pt_user_id,'') pt_user_id FROM cashier_info where cas_status = 1 and cas_id = " + mCashierInfo.getString("cas_id")))
        {
            final JSONObject st_info = getConnParam();
            mUrl = st_info.getString("server_url");
            mAppId = st_info.getString("appId");
            mAppSecret = st_info.getString("appSecret");
            mStoreInfo = JSON.parseObject(st_info.getString("storeInfo"));

            //启动心跳线程
            mSyncManagement.testNetwork();

            return true;
        }
        err.append(mCashierInfo.getString("info"));
        return false;
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
        return SQLiteHelper.isNotInit() || mStoreInfo == null || mCashierInfo == null;
    }

    public String getCashierName(){
        return Utils.getNullStringAsEmpty(mCashierInfo,"cas_name");
    }
    public String getPosNum(){return Utils.getNullStringAsEmpty(mCashierInfo,"pos_num");}
    public String getCashierId(){
        return Utils.getNullStringAsEmpty(mCashierInfo,"cas_id");
    }
    public String getCasPwd(){
        return Utils.getNullStringAsEmpty(mCashierInfo,"cas_pwd");
    }
    public String getCashierCode(){
        return Utils.getNullStringAsEmpty(mCashierInfo,"cas_code");
    }
    public String getPtUserId(){
        return Utils.getNullStringAsEmpty(mCashierInfo,"pt_user_id");
    }
    public String getStoreName(){
        return Utils.getNullStringAsEmpty(mStoreInfo,"stores_name");
    }
    public String getStoreTelephone(){
        return Utils.getNullStringAsEmpty(mStoreInfo,"telphone");
    }
    public String getStoreRegion(){
        return Utils.getNullStringAsEmpty(mStoreInfo,"region");
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

    public static String getNotEmptyHintsString(final String sz){
        return mApplication.getString(R.string.not_empty_hint_sz,sz);
    }
    public static String getNotExistHintsString(final String sz){
        return mApplication.getString(R.string.not_exist_hint_sz,sz);
    }


    private final ActivityLifecycleCallbacks activityCallbacks = new ActivityLifecycleCallbacks() {
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
            if (activity instanceof MessageCallback)mCallbacks.remove(activity);
            if (mActivities.isEmpty()){
                THREAD_POOL_EXECUTOR.shutdownNow();
                Logger.d("THREAD_POOL_EXECUTOR shutdown...");
                exit();
            }
        }
    };

    public void exit(){
        myhandler.removeCallbacksAndMessages(null);
        mSyncManagement.quit();
        mApplication = null;
        SQLiteHelper.closeDB();
        AppDatabase.closeDB();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * fastJson default configuration
     * */
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

    private void setNetworkStatus(boolean b){
        mNetworkStatus.set(b);
    }
    public boolean isConnection(){
        return mNetworkStatus.get();
    }
    public void registerHandleMessage(final MessageCallback callback){
        mCallbacks.add(callback);
    }
    public void unregisterHandleMessage(final MessageCallback callback){
        mCallbacks.remove(callback);
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

    public void reupload_retail_order(){
        mSyncManagement.sync_retail_order(true);
    }

    public void start_sync(){
        mSyncManagement.start_sync();
    }

    public void manualSync(){
        mSyncManagement.afresh_sync();
    }
    public void sync_refund_order(){
        mSyncManagement.sync_refund_order();
    }

    public void resetSync(){
        mSyncManagement.rest();
    }

    public void clearBasicsData(){
        final List<String> names = SQLiteHelper.getSyncDataTableName();
        final StringBuilder err = new StringBuilder();
        for (String name : names){
            if (SQLiteHelper.execDelete(name,null,null,err) < 0){
                MyDialog.toastMessage(err.toString());
            }
        }
    }
    public static void finishSync(){
        mApplication.myhandler.obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();
    }
    public static void transSuccess(){
        CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID,true);
    }
    public static void transFailure(){
        CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID,false);
    }
    public static void showSyncErrorMsg(final String msg){
        CustomApplication.sendMessage(MessageID.SYNC_ERR_ID,msg);
    }
    public static void showMsg(final String msg){
        CustomApplication.sendMessage(SYNC_DIS_INFO_ID,msg);
    }

    public static void runInMainThread(Runnable r){
        mApplication.myhandler.removeCallbacks(r);
        mApplication.myhandler.post(r);
    }

    public static void sendMessage(int what){
        mApplication.myhandler.obtainMessage(what).sendToTarget();
    }
    public static void sendMessage(int what,  Object obj){
        mApplication.myhandler.obtainMessage(what,obj).sendToTarget();
    }

    public static void sendMessageAtFrontOfQueue(int what){
        mApplication.myhandler.sendMessageAtFrontOfQueue(mApplication.myhandler.obtainMessage(what));
    }

    public static void postAtFrontOfQueue(Runnable r){
        mApplication.myhandler.removeCallbacks(r);
        mApplication.myhandler.postAtFrontOfQueue(r);
    }

    public static void postDelayed(Runnable r, long delayMillis){
        mApplication.myhandler.removeCallbacks(r);
        mApplication.myhandler.postDelayed(r,delayMillis);
    }

    public static float getDimension(@DimenRes int id){
        return mApplication.getResources().getDimension(id);
    }
    public static String getStringByResId(@StringRes int id,Object... formatArgs){
        return mApplication.getResources().getString(id,formatArgs);
    }

    private static class MyHandler extends Handler {
        private MyHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MessageID.NETWORKSTATUS_ID:
                    if ((msg.obj instanceof Boolean)){
                        boolean code = (boolean) msg.obj;
                        if (mApplication.getAndSetNetworkStatus(code) != code){
                            mApplication.executeMsg(msg);
                        }
                    }
                    break;
                case MessageID.TRANSFERSTATUS_ID:
                    if ((msg.obj instanceof Boolean)){
                        boolean code = (boolean) msg.obj;

                        if (mApplication.getAndSetTransferStatus(code) != code){
                            if (code){
                                msg.arg1 = 2;
                            }else
                                msg.arg1 = 3;
                        }else
                            msg.arg1 = 1;
                        mApplication.executeMsg(msg);
                    }
                    break;
                case MessageID.LOGIN_FINISH_ID:
                    mApplication.initModulePermission();
                    mApplication.setNetworkStatus(msg.obj instanceof Boolean && (boolean) msg.obj);
                    break;
                case MessageID.START_SYNC_ORDER_INFO_ID:
                    Toast.makeText(mApplication,"开始上传数据",Toast.LENGTH_SHORT).show();
                    break;
                case MessageID.FINISH_SYNC_ORDER_INFO_ID:
                    Toast.makeText(mApplication,"数据上传完成",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    mApplication.executeMsg(msg);
            }
        }
    }
    private void executeMsg(@NonNull Message msg){
        for (MessageCallback callback : mCallbacks){
            callback.handleMessage(myhandler,msg);
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
        if (CustomApplication.isPracticeMode() || !mApplication.getStoreId().equals(stores_id)){
            /*
            * 1、输入的门店ID和当前的不一样需要重新初始化数据库，因为数据库名称和门店ID相关。
            * 2、进入练习收银模式也需要切换到练习库，必须重新初始化数据库
            * */
            mApplication.resetSync();
            SQLiteHelper.closeDB();
            AppDatabase.closeDB();
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
                        MyDialog.toastMessage(sz);
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
                    final SimpleDateFormat sf = new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA);
                    final String sz_offline_time = String.format(Locale.CHINA,"离线时间已超过%d小时;最后离线时间:%s,当前时间:%s",max_offline_hours,sf.format(new Date(old_offline_time)),sf.format(new Date()));
                    MyDialog.displayErrorMessage(context,sz_offline_time);
                    code = false;
                }
            }
        }else {
            MyDialog.toastMessage(object.getString("info"));
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
                    MyDialog.toastMessage(object.getString("info"));
                }
            }
            if (null != sql ){
                if (!SQLiteHelper.execSql(object,sql)){
                    final String err = "更新离线时间错误:" + object.getString("v");
                    Logger.e(err);
                    MyDialog.toastMessage(err);
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
            MyDialog.toastMessage(err.toString());
        }
    }
}
