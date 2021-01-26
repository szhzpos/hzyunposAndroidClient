package com.wyc.cloudapp.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.wyc.cloudapp.fragment.AbstractBaseFragment;
import com.wyc.cloudapp.fragment.BaseParameterFragment;
import com.wyc.cloudapp.fragment.PeripheralSettingFragment;
import com.wyc.cloudapp.fragment.PrintFormatFragment;
import com.wyc.cloudapp.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class FragmentPagerAdapter<T extends AbstractBaseFragment> extends FragmentStateAdapter {
    private final List<T> mFragments;
    public FragmentPagerAdapter(@NonNull List<T> fragments,Fragment fragment) {
        this(fragments,fragment.getFragmentManager(),fragment.getLifecycle());
    }
    public FragmentPagerAdapter(@NonNull List<T> fragments,@NonNull FragmentActivity fragmentActivity){
        this(fragments,fragmentActivity.getSupportFragmentManager(),fragmentActivity.getLifecycle());
    }
    public FragmentPagerAdapter(@NonNull List<T> fragments,@NonNull FragmentManager fragmentManager,@NonNull Lifecycle lifecycle){
        super(fragmentManager,lifecycle);
        mFragments = fragments;
    }
    @Override
    protected void finalize(){
        Logger.d(getClass().getSimpleName() + " finalized");
    }

    @Override
    public @NonNull T createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    public T  getItem(int pos){
        return mFragments.get(pos);
    }

}