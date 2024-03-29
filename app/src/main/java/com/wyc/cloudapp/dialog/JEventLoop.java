package com.wyc.cloudapp.dialog;

import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public final class JEventLoop {
    private static final ThreadLocal<Stack<JEventLoop>> sThreadLocal = new ThreadLocal<>();
    private int mCode = 0;
    private Handler mHandler;
    private volatile boolean mDone;
    private final AtomicInteger mExec = new AtomicInteger();
    private final Object mLock = new Object();
    public JEventLoop(){
    }
    private static class ExitException extends RuntimeException{
        ExitException(){
            super();
        }
    }

    @Override
    protected void finalize(){
        Logger.d("JEventLoop finalized");
    }

    public int exec(){
        if (!mDone){
            Stack<JEventLoop> stack;
            synchronized (mLock){
                if (mDone){
                    mDone = false;
                    return mCode;
                }
                Looper looper = Looper.myLooper();
                if (mHandler == null) {
                    if (looper == null){
                        Looper.prepare();
                        looper = Looper.myLooper();
                    }
                    mHandler = new Handler(looper);
                }else {
                    if (mHandler.getLooper() != looper){
                        Logger.e("%s has already executed.",this);
                        return -1;
                    }
                }

                stack = sThreadLocal.get();
                if (stack == null){
                    stack = new Stack<>();
                    sThreadLocal.set(stack);
                }
                stack.push(this);
                Logger.d("%s线程exec,JEventLoop:<%s>,数量:%d",Thread.currentThread().getName(),this,stack.size());
            }

            //当mDone 为true时才退出循环，防止非当前对象退出
            while (!mDone){
                mExec.incrementAndGet();
                try {
                    Looper.loop();
                }catch (ExitException ignored){
                }
                mExec.decrementAndGet();
            }
            if (stack.pop() != this)throw new IllegalThreadStateException("JEventLoop internal error");
            Logger.d("%s线程exit,JEventLoop:<%s>,数量:%d,mCode:%d",Thread.currentThread().getName(),this,stack.size(),mCode);
        }
        mDone = false;
        return mCode;
    }

    public void done(int code){
        mCode = code;
        mDone = true;
        synchronized (mLock){
            if (mHandler != null){ mHandler.postAtFrontOfQueue(this::exit); }
        }
    }

    private void exit(){
        if (mExec.get() > 0)throw new ExitException();
    }
}
