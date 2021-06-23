package com.wyc.cloudapp.utils.http.callback;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSONException;
import com.wyc.cloudapp.logger.Logger;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class AbstractCallback<T> implements Callback {
    protected Handler mHandler;
    protected boolean isMainThread;

    protected AbstractCallback(boolean main){
        isMainThread = main;
        mHandler = new Handler(Looper.getMainLooper());
    }
    @Override
    public final void onFailure(@NotNull Call call, IOException e) {
        error(e.getMessage());
    }

    @Override
    public final void onResponse(@NotNull Call call, Response response) throws IOException {
        ResponseBody body = response.body();
        String content = null;
        if (body != null){
            content  = body.string();//Closes ResponseBody automatically
        }
        try {
            final T data = parseObject(content);
            if (isMainThread) {
                mHandler.post(()-> onSuccess(data));
            }else  onSuccess(data);
        } catch (JSONException e) {
            e.printStackTrace();
            error(e.getMessage() + "___" + content);
        }
    }
    private void error(final String msg){
        mHandler.post(()-> onError(msg));
    }

    protected abstract T parseObject(String c) throws JSONException;
    protected abstract void onError(String msg);
    protected abstract void onSuccess(T data);
}