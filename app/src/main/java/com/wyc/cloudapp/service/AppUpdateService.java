package com.wyc.cloudapp.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Locale;

public class AppUpdateService extends Service {
    public static final String APP_PROGRESS_BROADCAST = "com.wyc.cloudapp.download_progress";
    public static final int SUCCESS_STATUS = 1,ERROR_STATUS = 2,PROGRESS_STATUS = 3,INSTALL_STATUS = 4,BAD_NETWORK_STATUS = 5;
    private ServiceHandler mServiceHandler;
    private HandlerThread thread;

    public AppUpdateService() {
        super();
    }

    private final class ServiceHandler extends Handler {
        int  startId;
        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        protected void finalize(){
            Logger.d("%s finalized",getClass().getSimpleName());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MessageID.APP_CHECK_VER_ID) {
                startId = msg.arg1;
                if (msg.obj instanceof JSONObject) {
                    try {
                        check_ver((JSONObject) msg.obj);
                    }catch (JSONException e){
                        e.printStackTrace();
                        update_error(e.getLocalizedMessage());
                    }
                }
            }
        }
        private void check_ver(final JSONObject json) throws JSONException {
            final String base_url = Utils.getNullStringAsEmpty(json,"url"),appid = Utils.getNullStringAsEmpty(json,"appid"),
                    appSecret = Utils.getNullStringAsEmpty(json,"appSecret"),url = base_url + "/api/pos_upgrade/index";
            final Intent check_ver_intent = new Intent(APP_PROGRESS_BROADCAST) ;
            final HttpRequest httpRequest = new HttpRequest();

            final JSONObject object = new JSONObject();
            object.put("appid", appid);
            object.put("dir_name","pos_youren_an");
            final JSONObject retJson = HttpUtils.sendPost(url,HttpRequest.generate_request_parm(object,appSecret),30,true);
            switch (retJson.getIntValue("flag")) {
                case 0:
                    int rsCode = Utils.getNotKeyAsNumberDefault(retJson,"rsCode",-1);
                    if (rsCode == HttpURLConnection.HTTP_BAD_REQUEST || rsCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                        check_ver_intent.putExtra("status",BAD_NETWORK_STATUS);
                        sendBroadcast(check_ver_intent);
                    }else
                        update_error(retJson.getString("info"));
                    break;
                case 1:
                    final JSONObject info_json = JSON.parseObject(retJson.getString("info"));
                    switch (info_json.getString("status")) {
                        case "n":
                            update_error(info_json.getString("info"));
                            break;
                        case "y":
                            final JSONArray file_list = JSON.parseArray(Utils.getNullOrEmptyStringAsDefault(info_json,"file_list","[]"));
                            if (file_list.isEmpty()){
                                check_ver_intent.putExtra("status",SUCCESS_STATUS);
                                sendBroadcast(check_ver_intent);
                            }else {
                                final JSONObject file_info = file_list.getJSONObject(0);//可能抛出java.lang.IndexOutOfBoundsException异常
                                final String name = Utils.getNullStringAsEmpty(file_info,"name"),dwn_url = Utils.getNullStringAsEmpty(file_info,"dwn_url");
                                final int file_size = Utils.getNotKeyAsNumberDefault(file_info,"size",0);
                                int v_index = name.indexOf("v"),last_index = name.lastIndexOf(".");
                                if (v_index != -1 && last_index != -1){
                                    final String online_ver = name.substring(v_index + 1,last_index);
                                    Logger.d("file_name:%s,online_ver:%s",name,online_ver);

                                    try {
                                        final PackageInfo packageInfo = getPackageManager().getPackageInfo("com.wyc.cloudapp",0);
                                        long version_code = -1;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                            version_code = packageInfo.getLongVersionCode();
                                        }else {
                                            version_code = packageInfo.versionCode;
                                        }
                                        Logger.d("version_code:%d,versionName:%s",version_code,packageInfo.versionName);

                                        if (packageInfo.versionName.compareTo(online_ver) < 0){
                                            object.clear();
                                            object.put("appid",appid);
                                            downloadApkFile(httpRequest,dwn_url,HttpRequest.generate_request_parm(object,appSecret),file_size);
                                        }else {
                                            check_ver_intent.putExtra("status",SUCCESS_STATUS);
                                            sendBroadcast(check_ver_intent);
                                        }
                                    } catch (PackageManager.NameNotFoundException e) {
                                        e.printStackTrace();
                                        update_error(e.getMessage());
                                    }
                                }else {
                                    update_error(String.format(Locale.CHINA,"版本信息解析错误,文件名:%s",name));
                                }
                            }
                            break;
                    }
                    break;
            }
            stopSelf(startId);
        }

        private void downloadApkFile(final HttpRequest httpRequest,final String dwn_url,final String param,final long file_size){
            final String file_directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzposupdatefile",filepath = file_directory + "/app.apk";
            final File down_file = new File(filepath);
            if (down_file.exists()){
                down_file.delete();
            }
            if (initDirectory(file_directory)){
                final Intent intent = new Intent(APP_PROGRESS_BROADCAST);
                intent.putExtra("status",PROGRESS_STATUS);
                httpRequest.setRequestListener(size -> {
                    double percent = (double) size / (double)file_size;
                    intent.putExtra("Progress",percent);
                    sendBroadcast(intent);
                });
               final JSONObject retJson = httpRequest.getFileForPost(dwn_url,param,down_file);
                switch (retJson.getIntValue("flag")) {
                    case 0:
                        update_error(retJson.getString("info"));
                        break;
                    case 1:
                        intent.putExtra("status",INSTALL_STATUS);
                        intent.putExtra("filePath",filepath);
                        sendBroadcast(intent);
                        break;
                }
            }
        }

        private boolean initDirectory(final String path){
            final File file = new File(path);
            if (!file.exists()){
                if (!file.mkdir()){
                    update_error("初始化更新目录错误!");
                    return false;
                }
            }
            return true;
        }

        private void update_error(final String err){
            final Intent check_ver_intent = new Intent(APP_PROGRESS_BROADCAST) ;
            check_ver_intent.putExtra("info",err);
            check_ver_intent.putExtra("status",ERROR_STATUS);
            sendBroadcast(check_ver_intent);
            stopSelf(startId);
        }
    }

    @Override
    public void onCreate() {
        thread = new HandlerThread("AppUpdateService",Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceHandler = new ServiceHandler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String url = intent.getStringExtra("url"),appid = intent.getStringExtra("appid"),appSecret = intent.getStringExtra("appSecret");
        final JSONObject obj = new JSONObject();
        obj.put("url",url);
        obj.put("appid",appid);
        obj.put("appSecret",appSecret);
        mServiceHandler.obtainMessage(MessageID.APP_CHECK_VER_ID,startId,0,obj).sendToTarget();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        thread.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
