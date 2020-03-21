package com.wyc.cloudapp.network;

import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.handler.SyncHandler;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkManagement extends Thread {
    private CountDownLatch handlerInitLatch;
    private Handler mHandler;
    private SyncHandler mSyncHandler;
    private String mAppId,mAppScret,mUrl,mPosNum,mOperId,mStoresId;
    private NetworkManagement(Handler handler){
        this.mHandler = handler;
        handlerInitLatch = new CountDownLatch(1);
    }
    public NetworkManagement(Handler handler,final String url, final String appid, final String appscret,final String stores_id,final String pos_num, final String operid){
        this(handler);

        mUrl = url ;
        mAppId = appid;
        mAppScret = appscret;
        mPosNum = pos_num;
        mOperId = operid;
        mStoresId = stores_id;
    }
    @Override
    public void run(){
        if (mSyncHandler == null) {
            Looper.prepare();
            mSyncHandler = new SyncHandler(mHandler,mUrl,mAppId,mAppScret,mStoresId,mPosNum,mOperId);
            handlerInitLatch.countDown();
        }
        Looper.loop();
    }

    public SyncHandler getHandler(){
        try{
            handlerInitLatch.await();//必须确保Handler初始化
        }catch (InterruptedException e){
            e.fillInStackTrace();
        }
        return this.mSyncHandler;
    }

    public void quit(){
        if (mSyncHandler != null){
            mSyncHandler.stop();
            mSyncHandler.getLooper().quit();
            mSyncHandler = null;
            mHandler = null;
            handlerInitLatch = null;
        }
    }

    public void start_sync(boolean b){
        if (!isAlive())start();
        if (mSyncHandler == null)mSyncHandler = getHandler();
        mSyncHandler.setReportProgress(b);
        if (b){
            mSyncHandler.sync();
            mSyncHandler.syncFinish();
        }else{
            mSyncHandler.startNetworkTest();
        }
    }
}
