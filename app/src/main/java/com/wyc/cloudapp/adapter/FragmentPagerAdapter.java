package com.wyc.cloudapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.wyc.cloudapp.fragment.AbstractBaseFragment;
import com.wyc.cloudapp.fragment.BaseParameterFragment;
import com.wyc.cloudapp.fragment.PeripheralSettingFragment;
import com.wyc.cloudapp.fragment.PrintFormatFragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentPagerAdapter extends FragmentStateAdapter {
    private List<AbstractBaseFragment> mFragments;
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

    public AbstractBaseFragment getItem(int pos){
        return mFragments.get(pos);
    }

}