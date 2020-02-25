package com.wyc.cloudapp.handlerThread;

import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.handler.SyncHandler;

import java.util.concurrent.CountDownLatch;

public class SyncThread extends Thread {
    private CountDownLatch handlerInitLatch;
    private Handler SyncHandler,syncActivityHandler;
    public SyncThread(Handler handler){
         this.syncActivityHandler = handler;
         handlerInitLatch = new CountDownLatch(1);
    }
    @Override
    public void run() {
        if (SyncHandler == null) {
            Looper.prepare();
            SyncHandler = new SyncHandler(syncActivityHandler);
            handlerInitLatch.countDown();
        }
        Looper.loop();
    }
    public Handler getHandler(){
        try{
            handlerInitLatch.await();//必须确保Handler初始化
        }catch (InterruptedException e){
        }
        return this.SyncHandler;
    }
    public void quit(){
        if (SyncHandler != null){
            SyncHandler.removeCallbacksAndMessages(null);
            SyncHandler.getLooper().quit();
            SyncHandler = null;
            syncActivityHandler = null;
            handlerInitLatch = null;
        }
    }
}
