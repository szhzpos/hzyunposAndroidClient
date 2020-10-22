package com.wyc.cloudapp.activity.mobile;

import android.os.Bundle;

import com.wyc.cloudapp.R;

public class MobileNavigationActivity extends AbstractMobileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_navigation;
    }
}