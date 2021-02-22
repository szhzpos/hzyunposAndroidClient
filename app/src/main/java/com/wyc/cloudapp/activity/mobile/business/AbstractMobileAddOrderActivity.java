package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;

public abstract class AbstractMobileAddOrderActivity extends AbstractMobileActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);

        initTitle();
    }

    protected abstract JSONObject generateQueryCondition();

    private void initTitle(){
        final Intent intent = getIntent();
        if (intent != null)setMiddleText(intent.getStringExtra("title"));
    }

    private void query(final String id){

    }
}
