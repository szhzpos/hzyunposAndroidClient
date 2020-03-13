package com.wyc.cloudapp.network;

import android.os.Handler;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkManagement extends Thread {
    private Handler mHandler;
    private String mAppId,mAppScret,mNetworkStatusUrl,mPosNum,mOperId;
    //mTestNetworkStatusFlag是否检测网络状态标志 mNetworkStatusFlag当前网络状态标志 是否下载标志
    private AtomicBoolean mTestNetworkStatusFlag = new AtomicBoolean(true),mNetworkStatusFlag = new AtomicBoolean(true),mDownLoadFlag = new AtomicBoolean(true);
    private int mPreNeworkStatusCode = HttpURLConnection.HTTP_OK,mSyncInterval = 3000;//mSyncInterval 同步时间间隔，默认3秒
    private HttpRequest mHttp;

    private NetworkManagement(Handler handler){
        this.mHandler = handler;
    }
    public NetworkManagement(Handler handler, final String url, final String appid, final String appscret, final String pos_num, final String operid){
        this(handler);
        mNetworkStatusUrl = url + "/api/heartbeat/index";
        mAppId = appid;
        mAppScret = appscret;
        mPosNum = pos_num;
        mOperId = operid;
    }
    @Override
    public void run(){
        JSONObject data = new JSONObject(),retJson,info_json;
        final String prefix = "网络检测错误：";
        long interval = 0,resting_seed = 1000;
        int err_code = HttpURLConnection.HTTP_OK;
        mHttp = new HttpRequest();
        mHttp.setConnTimeOut(5000);
        try {
            data.put("appid",mAppId);
            data.put("pos_num",mPosNum);
            data.put("randstr", Utils.getNonce_str(8));
            data.put("cas_id",mOperId);

            while (mTestNetworkStatusFlag.get()){
                if (mDownLoadFlag.get() && (interval += resting_seed ) >= mSyncInterval){//数据下载

                    interval = 0;
                }else{//网络检测
                    sleep(resting_seed);
                    retJson = mHttp.sendPost(mNetworkStatusUrl,HttpRequest.generate_request_parm(data,mAppScret),true);
                    switch (retJson.optInt("flag")) {
                        case 0:
                            if (retJson.has("rsCode")){//flag为了0并且存在rsCode网络错误
                                err_code = retJson.getInt("rsCode");
                                if (mPreNeworkStatusCode != err_code){
                                    Logger.e("连接服务器错误：" + retJson.optString("info"));
                                }
                                updateNetworkstatus(false);
                            }else{
                                Logger.e("数据解析错误：" + retJson.optString("info"));
                            }
                            break;
                        case 1:
                            updateNetworkstatus(true);
                            err_code = retJson.getInt("rsCode");
                            if (mPreNeworkStatusCode != HttpURLConnection.HTTP_OK){//如果之前网络响应状态不为OK,则重连成功
                                Logger.i("重新连接服务器成功！");
                            }
                            info_json = new JSONObject(retJson.getString("info"));
                            switch (info_json.getString("status")){
                                case "n":
                                    updateNetworkstatus(false);
                                    Logger.e(prefix + retJson.optString("info"));
                                    break;
                                case "y":
                                    //Logger.json(info_json.toString());
                                    break;
                            }
                            break;
                    }
                    mPreNeworkStatusCode = err_code;
                }
            }
            //退出循环后在进行一次检查设置，以保证线程退出时，状态为false
            mNetworkStatusFlag.compareAndSet(true,false);
            mDownLoadFlag.compareAndSet(true,false);
        } catch (JSONException | InterruptedException e) {
            Logger.e("检测网络错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateNetworkstatus(boolean status){
        mNetworkStatusFlag.set(status);
        if (mSyncInterval > 0){
            mDownLoadFlag.compareAndSet(!status,status);
        }
        mHandler.obtainMessage(MessageID.NETWORKSTATUS_ID,status).sendToTarget();
    }
    private void updateSyncErrStatus(int code){
        switch (code) {
            case 0://下载业务错误
                mDownLoadFlag.compareAndSet(true,false);
                mHandler.obtainMessage(MessageID.DOWNLOADSTATUS_ID).sendToTarget();
                break;
            case 1:// 网络错误
                updateNetworkstatus(false);
                break;
            case 2://上传业务错误

                break;
        }
    }

    public void start_sync(){
        if (mSyncInterval > 0){
            mDownLoadFlag.compareAndSet(false,true);
        }
        if (!isAlive())start();
    }

    public void stop_sync(){
        if(mTestNetworkStatusFlag.compareAndSet(true,false)){
            if (mHttp != null)mHttp.clearConnection(HttpRequest.CLOSEMODE.POST);
            mDownLoadFlag.compareAndSet(true,false);
            mNetworkStatusFlag.compareAndSet(true,false);
            try {
                this.join();
            } catch (InterruptedException e) {
                Logger.e("停止同步错误：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
