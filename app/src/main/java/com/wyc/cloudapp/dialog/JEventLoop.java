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
    private volatile boolean mDone,mExec;
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

            //当mDone 为true时才退出循环，防止非当前对象退出
            while (!mDone){
                mExec = true;
                try {
                    Looper.loop();
                }catch (ExitException ignored){
                }
            }
            mExec = false;

            if (stack.pop() != this)throw new IllegalThreadStateException("JEventLoop internal error");
            Logger.d("%s线程exit,JEventLoop:<%s>,数量:%d,mCode:%d",Thread.currentThread().getName(),this,stack.size(),mCode);
            if (!stack.isEmpty()){
                final JEventLoop loop = stack.peek();
                //如果栈顶对象的mDone为真，在对象mHandler所属的消息队列最前面加入退出事件。当当前循环退出后立即让栈顶对象退出。
                if (loop.mDone)if (loop.mHandler != null){ loop.mHandler.postAtFrontOfQueue(loop::exit);}
            }
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
        if (mExec)throw new ExitException();
    }
}
