package com.wyc.cloudapp.dialog;

import android.os.Handler;
import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;

import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public final class JEventLoop {
    private static final ThreadLocal<Stack<JEventLoop>> sThreadLocal = new ThreadLocal<>();
    private int mCode = 0;
    private Handler mHandler;
    private AtomicBoolean mDone;
    private final Object mLock = new Object();
    public JEventLoop(){
        mDone = new AtomicBoolean(false);
    }
    private class ExitException extends RuntimeException{
        ExitException(){
            super();
        }
    }

    public int exec(){
        if (!mDone.get()){
            synchronized (mLock){
                if (mDone.get())return mCode;

                Stack<JEventLoop> stack = sThreadLocal.get();
                if (stack == null){
                    stack = new Stack<>();
                    sThreadLocal.set(stack);
                }
                stack.push(this);
                Logger.d("%s线程exec,JEventLoop:<%s>,数量:%d",Thread.currentThread().getName(),this,stack.size());
                if (Looper.myLooper() == null)Looper.prepare();
                mHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
            }
            try {
                Looper.loop();
            }catch (ExitException ignored){
            }
        }
        return mCode;
    }

    public void done(int code){
        mCode = code;
        mDone.set(true);
        synchronized (mLock){
            if (mHandler != null){
                mHandler.post(this::exit);
                mHandler = null;
            }
        }
    }

    private void exit(){
        final Stack<JEventLoop> stack = sThreadLocal.get();
        if (stack != null && !stack.isEmpty()){
            if (stack.pop() != this)throw new IllegalThreadStateException("JEventLoop internal error");
            Logger.d("%s线程exit,JEventLoop:<%s>,数量:%d,mCode:%d",Thread.currentThread().getName(),this,stack.size(),mCode);
            throw new ExitException();
        }
    }
}
