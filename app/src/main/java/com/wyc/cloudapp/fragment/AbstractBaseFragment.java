package com.wyc.cloudapp.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wyc.cloudapp.activity.base.IHookKey;
import com.wyc.cloudapp.logger.Logger;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class AbstractBaseFragment extends Fragment implements IHookKey {
    private View mRootView;
    protected abstract void viewCreated();

    @Override
    protected void finalize(){
        Logger.d(getClass().getSimpleName() + " finalized");
    }
    @CallSuper
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        viewCreated();
    }
    public final @Nullable <T extends View> T findViewById(int id) {
        if (mRootView == null) {
            return null;
        }
        return mRootView.findViewById(id);
    }
    protected View getRootView(){
        return mRootView;
    }

    public boolean onBackPressed(){
        return true;
    }

    @Override
    public boolean hookEnterKey() {
        return false;
    }
}
