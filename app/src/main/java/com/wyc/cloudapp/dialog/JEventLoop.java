package com.wyc.cloudapp.dialog;

import android.os.Looper;

import com.wyc.cloudapp.logger.Logger;

import java.util.Stack;

import static android.os.Looper.myLooper;

public final class JEventLoop {
    private static final ThreadLocal<Stack<JEventLoop>> sThreadLocal = new ThreadLocal<>();
    private int mCode = 0;
    public JEventLoop(){

    }

    private static class ExitException extends RuntimeException{
        ExitException(){
            super();
        }
    }

    public int exec(){
        Stack<JEventLoop> stack = sThreadLocal.get();
        if (stack == null){
            stack = new Stack<>();
            sThreadLocal.set(stack);
        }
        stack.push(this);
        Logger.d("%s线程exec,JEventLoop实例数量:%d",Thread.currentThread().getName(),stack.size());
        final Looper me = myLooper();
        if (me == null)Looper.prepare();
        try {
            Looper.loop();
        }catch (ExitException ignored){
        }
        return mCode;
    }

    public void done(int code){
        Stack<JEventLoop> stack = sThreadLocal.get();
        if (stack != null && !stack.isEmpty()){
            final JEventLoop eventLoop = stack.pop();
            if (eventLoop != this)throw new IllegalThreadStateException("JEventLoop internal error");
            mCode = code;
            Logger.d("%s线程exit,JEventLoop实例数量:%d,mCode:%d",Thread.currentThread().getName(),stack.size(),code);
            throw new ExitException();
        }
    }
}
