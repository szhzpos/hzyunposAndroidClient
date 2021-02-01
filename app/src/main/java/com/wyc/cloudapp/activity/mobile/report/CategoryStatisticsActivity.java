package com.wyc.cloudapp.activity.mobile.report;

import android.os.Bundle;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;

public final class CategoryStatisticsActivity extends AbstractMobileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getStoreName());

    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_category_statistics;
    }
}