package com.wyc.cloudapp.fragment;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class AbstractBaseFragment extends Fragment {
    protected View mRootView;
    protected Context mContext;
    public AbstractBaseFragment() {}
    public abstract String getTitle();
    public abstract JSONObject loadContent();
    public abstract boolean saveContent();
}
