package com.wyc.cloudapp.utils.http.callback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.TypeReference;

import java.util.List;

public abstract class ArrayCallback<T> extends TypeCallback<ArrayResult<T>> {
    protected ArrayCallback(Class<T> c,boolean main) {
        super(new TypeReference<ArrayResult<T>>(c){}.getType(),main);
    }
    @Override
    protected final void onSuccess(ArrayResult<T> data) {
        if (data.isSuccess()){
            onSuccessForResult(data.getData(),data.getInfo());
        }else onError(data.getInfo());
    }
    protected abstract void onSuccessForResult(@Nullable List<T> d, final String hint);
}
