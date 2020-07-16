package com.wyc.cloudapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.io.File;
public class AppUpdateService extends Service {
    public static final String APP_PROGRESS_BROADCAST = "com.wyc.cloudapp.download_progress";
    public static final int SUCCESS_STATUS = 1,ERROR_STATUS = 2,PROGRESS_STATUS = 3;
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
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MessageID.APP_CHECK_VER_ID:
                    startId = msg.arg1;
                    if (msg.obj instanceof JSONObject){
                        check_ver((JSONObject) msg.obj);
                    }
                    break;
                case MessageID.APP_UPDATE_ID:

                    break;
                case MessageID.APP_UPDATESERVICE_EXIT_ID:
                    stopSelf(startId);
                    break;
            }
        }
        private void check_ver(final JSONObject json){

            final String base_url = Utils.getNullStringAsEmpty(json,"url"),appid = Utils.getNullStringAsEmpty(json,"appid"),
                    appSecret = Utils.getNullStringAsEmpty(json,"appSecret"),url = base_url + "/api/pos_upgrade/index";

            final JSONObject object = new JSONObject();
            object.put("appid", appid);
            object.put("dir_name","pos_youren_an");

            final HttpRequest httpRequest = new HttpRequest();
            JSONObject retJson = httpRequest.setConnTimeOut(3000).setReadTimeOut(3000).sendPost(url,HttpRequest.generate_request_parm(object,appSecret), true);
            Logger.d_json(retJson.toString());
            switch (retJson.getIntValue("flag")) {
                case 0:
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
                                final Intent check_ver_intent = new Intent(APP_PROGRESS_BROADCAST) ;
                                check_ver_intent.putExtra("status",SUCCESS_STATUS);
                                sendBroadcast(check_ver_intent);
                            }else {
                                final JSONObject file_info = file_list.getJSONObject(0);



                                final String name = Utils.getNullStringAsEmpty(file_info,"name"),dwn_url = Utils.getNullStringAsEmpty(file_info,"dwn_url");
                                final int file_size = Utils.getNotKeyAsNumberDefault(file_info,"size",0);

                                object.clear();
                                object.put("appid",appid);

                                downloadApkFile(httpRequest,dwn_url,HttpRequest.generate_request_parm(object,appSecret),file_size);
                            }
                            break;
                    }
                    break;
            }
        }

        private void downloadApkFile(final HttpRequest httpRequest,final String dwn_url,final String param,final long file_size){
            final String file_directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzposupdatefile",filepath = file_directory + "/app-release.apk";
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
               Logger.d_json(retJson.toString());
                switch (retJson.getIntValue("flag")) {
                    case 0:
                        update_error(retJson.getString("info"));
                        break;
                    case 1:
                        intent.putExtra("status",SUCCESS_STATUS);
                        sendBroadcast(intent);

                        installAPK(getBaseContext(),filepath);
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

        //下载到本地后执行安装
        private void installAPK(Context mContext,String filepath) {
            Uri data ;
            final Intent intent= new Intent(Intent.ACTION_VIEW);
            final File file =  new File(filepath);

            // 判断版本大于等于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // "com.wyc.cloudapp.fileprovider"即是在清单文件中配置的authorities
                data = FileProvider.getUriForFile(mContext, "com.wyc.cloudapp.fileprovider",file);
                // 给目标应用一个临时授权
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                data = Uri.fromFile(file);
            }
            intent.setDataAndType(data, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
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
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        final String url = intent.getStringExtra("url"),appid = intent.getStringExtra("appid"),appSecret = intent.getStringExtra("appSecret");
        final JSONObject obj = new JSONObject();
        obj.put("url",url);
        obj.put("appid",appid);
        obj.put("appSecret",appSecret);
        mServiceHandler.obtainMessage(MessageID.APP_CHECK_VER_ID,startId,0,obj).sendToTarget();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        thread.quit();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
