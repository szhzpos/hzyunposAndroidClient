package com.wyc.cloudapp.network.sync;

import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class SyncManagement extends Thread {
    private CountDownLatch handlerInitLatch;
    private Handler mMainActivityHandler;
    private SyncHandler mSyncHandler;
    private String mAppId, mAppSecret,mUrl,mPosNum,mOperId,mStoresId;
    private SyncManagement(Handler handler){
        this.mMainActivityHandler = handler;
        handlerInitLatch = new CountDownLatch(1);
    }
    public SyncManagement(Handler handler, final String url, final String appid, final String appsecret, final String stores_id, final String pos_num, final String operid){
        this(handler);
        mUrl = url ;
        mAppId = appid;
        mAppSecret = appsecret;
        mPosNum = pos_num;
        mOperId = operid;
        mStoresId = stores_id;

        start();
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

    @Override
    public void run(){
        Logger.i("SyncManagement<%s>启动:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.CHINA).format(new Date()));
        if (mSyncHandler == null) {
            Looper.prepare();
            mSyncHandler = new SyncHandler(mMainActivityHandler,this);
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

    public void quit(){
        if (mSyncHandler != null){
            mSyncHandler.stop();
            try {//等待线程退出
                join(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mSyncHandler = null;
            mMainActivityHandler = null;
            handlerInitLatch = null;
        }
        Logger.i("SyncManagement<%s>退出:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(new Date()));
    }

    public void start_sync(boolean b){
        acquireHandler();
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

    public void sync_order_info(){
        sync_refund_order();

        sync_transfer_order();

        sync_retail_order();
    }

    public void sync_retail_order(){
        acquireHandler();
        mSyncHandler.startUploadRetailOrder();
    }

    public void sync_transfer_order(){
        acquireHandler();
        mSyncHandler.startUploadTransferOrder();
    }

    public void sync_refund_order(){
        acquireHandler();
        mSyncHandler.startUploadRefundOrder();
    }

    public void pauseSync(){
        acquireHandler();
        mSyncHandler.pause();
    }
    public void continueSync(){
        acquireHandler();
        mSyncHandler._continue();
    }

    private void acquireHandler(){
        if (mSyncHandler == null)mSyncHandler = getHandler();
    }
}
