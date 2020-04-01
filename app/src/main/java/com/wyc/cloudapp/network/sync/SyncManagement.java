package com.wyc.cloudapp.network.sync;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class SyncManagement extends Thread {
    private CountDownLatch handlerInitLatch;
    private Handler mHandler;
    private SyncHandler mSyncHandler;
    private String mAppId,mAppScret,mUrl,mPosNum,mOperId,mStoresId;
    private SyncManagement(Handler handler){
        this.mHandler = handler;
        handlerInitLatch = new CountDownLatch(1);
    }
    public SyncManagement(Handler handler, final String url, final String appid, final String appscret, final String stores_id, final String pos_num, final String operid){
        this(handler);
        mUrl = url ;
        mAppId = appid;
        mAppScret = appscret;
        mPosNum = pos_num;
        mOperId = operid;
        mStoresId = stores_id;

        start();
    }
    @SuppressLint("SimpleDateFormat")
    @Override
    public void run(){
        Logger.i("SyncManagement<%s>启动:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date()));
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
            e.printStackTrace();
        }
        return this.mSyncHandler;
    }

    @SuppressLint("SimpleDateFormat")
    public void quit(){
        if (mSyncHandler != null){
            mSyncHandler.stop();
            try {//等待线程退出
                join(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSyncHandler = null;
            mHandler = null;
            handlerInitLatch = null;
        }
        Logger.i("SyncManagement<%s>退出:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date()));
    }

    public void start_sync(boolean b){
        if (mSyncHandler == null)mSyncHandler = getHandler();
        if (b){
            mSyncHandler.modifyReportProgressStatus(true);
            mSyncHandler.sign_downloaded();
            mSyncHandler.sync();
            mSyncHandler.obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();//最后发送同步完成消息;
        }else{
            mSyncHandler.startNetworkTest();
        }
    }

    public void stop_sync(){
        mSyncHandler.stopSync();
    }

    public void sync_order(){
        if (mSyncHandler == null)mSyncHandler = getHandler();
        mSyncHandler.startUploadOrder();
    }
    public void pauseSync(){
        if (mSyncHandler == null)mSyncHandler = getHandler();
        mSyncHandler.pause();
    }
    public void continueSync(){
        if (mSyncHandler == null)mSyncHandler = getHandler();
        mSyncHandler._continue();
    }
}
