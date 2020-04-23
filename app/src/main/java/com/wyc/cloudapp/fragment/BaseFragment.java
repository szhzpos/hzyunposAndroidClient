package com.wyc.cloudapp.fragment;

import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment {
    public BaseFragment() {}
    public abstract String getTitle();
    public abstract JSONObject laodContent();
    public abstract boolean saveContent();
}
