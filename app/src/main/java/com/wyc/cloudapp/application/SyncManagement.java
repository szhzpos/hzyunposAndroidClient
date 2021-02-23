package com.wyc.cloudapp.application;

import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

class SyncManagement extends Thread {
    private CountDownLatch handlerInitLatch;
    private SyncHandler mSyncHandler;
    SyncManagement(){
        handlerInitLatch = new CountDownLatch(1);
        start();
    }

    void initSync(final String url, final String appid, final String appsecret, final String stores_id, final String pos_num, final String operid){
        acquireHandler();
        mSyncHandler.initParameter(url,appid,appsecret,stores_id,pos_num,operid);
    }

    @Override
    public void run(){
        Logger.i("SyncManagement<%s>启动:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.CHINA).format(new Date()));
        if (mSyncHandler == null) {
            Looper.prepare();
            mSyncHandler = new SyncHandler(Looper.myLooper());
            handlerInitLatch.countDown();
        }
        Looper.loop();
    }

    private SyncHandler getHandler(){
        try{
            handlerInitLatch.await();//必须确保Handler初始化
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return this.mSyncHandler;
    }

    void quit(){
        if (mSyncHandler != null){
            mSyncHandler.stop();
            try {//等待线程退出
                join(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSyncHandler = null;
            handlerInitLatch = null;
        }
        Logger.i("SyncManagement<%s>退出:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(new Date()));
    }
    void afresh_sync(){
        acquireHandler();
        mSyncHandler.sign_downloaded();
        start_sync(true);
    }
    void start_sync(boolean b){
        acquireHandler();
        if (b){
            mSyncHandler.modifyReportProgressStatus(true);
            mSyncHandler.sync();
            mSyncHandler.obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();//最后发送同步完成消息;
        }else{
            mSyncHandler.startNetworkTest();
        }
    }

    void stop_sync(){
        acquireHandler();
        mSyncHandler.stopSync();
    }

    void sync_order_info(){
        acquireHandler();
        mSyncHandler.sync_order_info();
    }

    void sync_retail_order(){
        acquireHandler();
        mSyncHandler.startUploadRetailOrder();
    }

    void sync_transfer_order(){
        acquireHandler();
        mSyncHandler.startUploadTransferOrder();
    }

    void sync_refund_order(){
        acquireHandler();
        mSyncHandler.startUploadRefundOrder();
    }

    void pauseSync(){
        acquireHandler();
        mSyncHandler.pause();
    }
    void continueSync(){
        acquireHandler();
        mSyncHandler._continue();
    }

    void rest(){
        acquireHandler();
        mSyncHandler.removeCallbacksAndMessages(null);
    }
    List<String> getDataTableName(){
        acquireHandler();
        return mSyncHandler.getSyncDataTableName();
    }

    private void acquireHandler(){
        if (mSyncHandler == null)mSyncHandler = getHandler();
    }
}
