package com.wyc.cloudapp.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.wyc.cloudapp.fragment.BaseFragment;
import com.wyc.cloudapp.fragment.BaseParameterFragment;
import com.wyc.cloudapp.fragment.PeripheralSettingFragment;
import com.wyc.cloudapp.fragment.PrintFormatFragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentPagerAdapter extends FragmentStateAdapter {
    private List<BaseFragment> mFragments;
    public FragmentPagerAdapter(Fragment fragment) {
        super(fragment);
        mFragments = new ArrayList<>();
        mFragments.add(new BaseParameterFragment());
        mFragments.add(new PeripheralSettingFragment());
        mFragments.add(new PrintFormatFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    public BaseFragment getItem(int pos){
        return mFragments.get(pos);
    }

}