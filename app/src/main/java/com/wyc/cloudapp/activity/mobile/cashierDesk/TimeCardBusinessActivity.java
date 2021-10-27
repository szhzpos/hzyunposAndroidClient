package com.wyc.cloudapp.activity.mobile.cashierDesk;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.mobileFragemt.AbstractMobileFragment;
import com.wyc.cloudapp.mobileFragemt.TimeCardSaleFragment;
import com.wyc.cloudapp.mobileFragemt.TimeCardUseFragment;

import java.util.ArrayList;
import java.util.List;

/*次卡业务*/
public class TimeCardBusinessActivity extends TimeCardBusinessBase {
    @NonNull
    @Override
    protected List<AbstractMobileFragment> createFragments() {
        final List<AbstractMobileFragment> fragments = new ArrayList<>();
        fragments.add(new TimeCardSaleFragment());
        fragments.add(new TimeCardUseFragment());
        return fragments;
    }
}