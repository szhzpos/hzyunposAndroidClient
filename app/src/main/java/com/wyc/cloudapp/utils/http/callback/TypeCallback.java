package com.wyc.cloudapp.utils.http.callback;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.logger.Logger;

import java.lang.reflect.Type;

public abstract class TypeCallback<T> extends AbstractCallback<T> {
    private final Type type;
    public TypeCallback(Type t){
        super(true);
        type = t;
    }
    public TypeCallback(Type t,boolean main) {
        super(main);
        type = t;
    }

    @Override
    protected T parseObject(String c) {
        return JSONObject.parseObject(c,type);
    }

}
