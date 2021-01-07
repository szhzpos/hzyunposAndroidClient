package com.wyc.cloudapp.application;

import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

class SyncManagement extends Thread {
    private CountDownLatch handlerInitLatch;
    private Handler mNotifyHandler;
    private SyncHandler mSyncHandler;
    private String mAppId, mAppSecret,mUrl,mPosNum,mOperId,mStoresId;
    SyncManagement(final Handler handler){
        mNotifyHandler = handler;
        handlerInitLatch = new CountDownLatch(1);
    }

    String getUrl(){
        return mUrl;
    }

    String getAppId(){
        return mAppId;
    }

    String getAppSecret(){
        return mAppSecret;
    }
    String getPosNum(){
        return mPosNum;
    }
    String getOperId(){
        return mOperId;
    }
    String getStoresId(){
        return mStoresId;
    }

    void initSync(final String url, final String appid, final String appsecret, final String stores_id, final String pos_num, final String operid){
        mUrl = url ;
        mAppId = appid;
        mAppSecret = appsecret;
        mPosNum = pos_num;
        mOperId = operid;
        mStoresId = stores_id;

        if (!isAlive())start();
    }

    @Override
    public void run(){
        Logger.i("SyncManagement<%s>启动:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.CHINA).format(new Date()));
        if (mSyncHandler == null) {
            Looper.prepare();
            mSyncHandler = new SyncHandler(Looper.myLooper(),mNotifyHandler,this);
            handlerInitLatch.countDown();
        }
        Looper.loop();
    }

    SyncHandler getHandler(){
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
            if (mNotifyHandler != null){
                mNotifyHandler.removeCallbacksAndMessages(null);
                mNotifyHandler = null;
            }
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
        mSyncHandler.stopSync();
    }

    void sync_order_info(){
        sync_refund_order();

        sync_transfer_order();

        sync_retail_order();

        if (mNotifyHandler != null) mNotifyHandler.sendMessageAtFrontOfQueue(mNotifyHandler.obtainMessage(MessageID.START_SYNC_ORDER_INFO_ID));
        if (mNotifyHandler != null) mNotifyHandler.obtainMessage(MessageID.FINISH_SYNC_ORDER_INFO_ID).sendToTarget();
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

    private void acquireHandler(){
        if (mSyncHandler == null)mSyncHandler = getHandler();
    }
}
