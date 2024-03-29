package com.wyc.cloudapp.activity.mobile.cashierDesk;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.mobileFragemt.AbstractMobileFragment;
import com.wyc.cloudapp.mobileFragemt.TimeCardSaleQueryFragment;
import com.wyc.cloudapp.mobileFragemt.TimeCardUseQueryFragment;

import java.util.ArrayList;
import java.util.List;

public class TimeCardQueryActivity extends FragmentContainerActivity<AbstractMobileFragment> {
    @NonNull
    @Override
    protected List<AbstractMobileFragment> createFragments() {
        final List<AbstractMobileFragment> fragments = new ArrayList<>();
        fragments.add(new TimeCardSaleQueryFragment());
        fragments.add(new TimeCardUseQueryFragment());
        return fragments;
    }
}