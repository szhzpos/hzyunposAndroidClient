package com.wyc.cloudapp.utils.http.callback;

import com.alibaba.fastjson.TypeReference;


public abstract class ObjectCallback<T> extends TypeCallback<ObjectResult<T>> {
    protected ObjectCallback(Class<T> c,boolean main) {
        super(new TypeReference<ObjectResult<T>>(c){}.getType(),main);
    }

    @Override
    protected final void onSuccess(ObjectResult<T> data) {
        if (data.isSuccess()){
            onSuccessForResult(data.getData(),data.getInfo());
        }else onError(data.getInfo());
    }
    protected abstract void onSuccessForResult(T d,final String hint);
}
