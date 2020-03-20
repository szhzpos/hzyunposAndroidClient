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
    private Handler mHandler,mSyncHandler;
    private String mAppId,mAppScret,mUrl,mPosNum,mOperId,mStoresId;
    private Timer mTimer;
    private int mSyncInterval = 1500;//mSyncInterval 同步时间间隔，默认3秒
    private boolean mReportProgress;
    private NetworkManagement(Handler handler){
        this.mHandler = handler;
        handlerInitLatch = new CountDownLatch(1);
    }
    public NetworkManagement(Handler handler,boolean report,final String url, final String appid, final String appscret,final String stores_id,final String pos_num, final String operid){
        this(handler);
        mReportProgress = report;
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
            mSyncHandler = new SyncHandler(mHandler,mReportProgress,mUrl,mAppId,mAppScret,mStoresId,mPosNum,mOperId);
            handlerInitLatch.countDown();
        }
        Looper.loop();
    }

    public Handler getHandler(){
        try{
            handlerInitLatch.await();//必须确保Handler初始化
        }catch (InterruptedException e){
            e.fillInStackTrace();
        }
        return this.mSyncHandler;
    }

    public void quit(){
        stopTimer();
        if (mSyncHandler != null){
            ((SyncHandler)mSyncHandler).stop();
            mSyncHandler.getLooper().quit();
            mSyncHandler = null;
            mHandler = null;
            handlerInitLatch = null;
        }
    }

    private void sync(){
        if (mSyncHandler == null)mSyncHandler = getHandler();
        mSyncHandler.obtainMessage(MessageID.SYNC_CASHIER_ID).sendToTarget();//收银员
        mSyncHandler.obtainMessage(MessageID.SYNC_GOODS_CATEGORY_ID).sendToTarget();//商品类别
        mSyncHandler.obtainMessage(MessageID.SYNC_GOODS_ID).sendToTarget();//商品信息
        mSyncHandler.obtainMessage(MessageID.SYNC_PAY_METHOD_ID).sendToTarget();//支付方式
        mSyncHandler.obtainMessage(MessageID.SYNC_STORES_ID).sendToTarget();//仓库信息
        mSyncHandler.obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();//最后发送同步完成消息
    }

    public void start_sync(boolean b){
        if (!isAlive())start();
        if (b){
            sync();
        }else{
            starTimer();
        }
    }

    private void starTimer(){
        if (mTimer == null){
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                long mlossTmie = 0;
                @Override
                public void run() {
                    if (System.currentTimeMillis() - mlossTmie >= mSyncInterval){
                        mlossTmie = System.currentTimeMillis();
                        sync();
                    }else{
                        if (mSyncHandler != null && !mSyncHandler.hasMessages(MessageID.NETWORKSTATUS_ID)){
                            mSyncHandler.obtainMessage(MessageID.NETWORKSTATUS_ID).sendToTarget();
                        }
                    }
                }
            },0,1000);
        }
    }

    private void stopTimer(){
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
}
