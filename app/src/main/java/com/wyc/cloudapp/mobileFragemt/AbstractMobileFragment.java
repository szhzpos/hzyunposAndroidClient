package com.wyc.cloudapp.mobileFragemt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;

import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.fragment.AbstractBaseFragment;

public abstract class AbstractMobileFragment extends AbstractBaseFragment {
    protected MainActivity mContext;
    public AbstractMobileFragment(final MainActivity activity){
        mContext = activity;
    }
    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getRootLayout(), container, false);
    }
    abstract protected int getRootLayout();
}
