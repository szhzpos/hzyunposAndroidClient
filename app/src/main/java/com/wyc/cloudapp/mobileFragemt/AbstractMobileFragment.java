package com.wyc.cloudapp.mobileFragemt;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.fragment.AbstractBaseFragment;

import butterknife.ButterKnife;

public abstract class AbstractMobileFragment extends AbstractBaseFragment {
    protected MainActivity mContext;
    public AbstractMobileFragment(){
    }
    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getRootLayout(), container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @CallSuper
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity){
            mContext = (MainActivity) context;
        }else throw new IllegalArgumentException("attach context is not instance of MainActivity...!");
    }
    abstract protected int getRootLayout();
}
