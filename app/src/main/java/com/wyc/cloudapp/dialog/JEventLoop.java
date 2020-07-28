package com.wyc.cloudapp.dialog;

import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;

import java.util.Objects;
import java.util.Stack;

public final class JEventLoop {
    private static final ThreadLocal<Stack<JEventLoop>> sThreadLocal = new ThreadLocal<>();
    private int mCode = 0;
    private Handler mHandler;
    private volatile boolean mDone = false;
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

                stack = sThreadLocal.get();
                if (stack == null){
                    stack = new Stack<>();
                    sThreadLocal.set(stack);
                }
                stack.push(this);
                Logger.d("%s线程exec,JEventLoop:<%s>,数量:%d",Thread.currentThread().getName(),this,stack.size());
                Looper looper = Looper.myLooper();
                if (looper == null){
                    Looper.prepare();
                    looper = Looper.myLooper();
                }
                if (mHandler == null || mHandler.getLooper() != looper) {
                    assert looper != null;
                    mHandler = new Handler(looper);
                }
            }
            try {
                Looper.loop();
            }catch (ExitException ignored){
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
            if (mHandler != null){ mHandler.post(this::exit); }
        }
    }

    private void exit(){
        throw new ExitException();
    }
}
