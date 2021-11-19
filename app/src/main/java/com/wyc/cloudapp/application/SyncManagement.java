package com.wyc.cloudapp.application;

import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class SyncManagement extends Thread {
    private SyncHandler1 mSyncHandler;
    SyncManagement(){
        start();
    }

    @Override
    public void run(){
        Logger.i("SyncManagement<%s>启动:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.CHINA).format(new Date()));
        if (mSyncHandler == null) {
            Looper.prepare();
            mSyncHandler = new SyncHandler1(Looper.myLooper());
        }
        Looper.loop();
    }
    void quit(){
        if (mSyncHandler != null){
            mSyncHandler.stop();
            try {//等待线程退出
                join(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                mSyncHandler.removeCallbacksAndMessages(null);
                mSyncHandler.getLooper().quit();
            }
            mSyncHandler = null;
        }
        Logger.i("SyncManagement<%s>退出:%s",getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(new Date()));
    }

    void afresh_sync(){
        if (mSyncHandler != null){
            mSyncHandler.sign_downloaded();
            start_sync();
        }
    }
    void start_sync(){
        if (mSyncHandler != null){
            mSyncHandler.syncAllBasics();
        }
    }
    void testNetwork(){
        if (mSyncHandler != null)mSyncHandler.startTestNetwork();
    }

    void sync_order_info(){
        if (mSyncHandler != null)mSyncHandler.sync_order_info();
    }

    void sync_retail_order(boolean reupload){
        if (mSyncHandler != null)mSyncHandler.startUploadRetailOrder(reupload);
    }

    void sync_transfer_order(){
        if (mSyncHandler != null)mSyncHandler.startUploadTransferOrder();
    }

    void sync_refund_order(){
        if (mSyncHandler != null)mSyncHandler.startUploadRefundOrder();
    }

    void rest(){
        if (mSyncHandler != null)mSyncHandler.removeCallbacksAndMessages(null);
    }

}
