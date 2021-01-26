package com.wyc.cloudapp.fragment;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;

public abstract class AbstractParameterFragment extends AbstractBaseFragment {
    protected Context mContext;
    public abstract String getTitle();
    public abstract JSONObject loadContent();
    public abstract boolean saveContent();

}
