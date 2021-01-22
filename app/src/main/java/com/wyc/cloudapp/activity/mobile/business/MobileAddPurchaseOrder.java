package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;

public class MobileAddPurchaseOrder extends AbstractMobileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTitle();
    }

    private void initTitle(){
        final Intent intent = getIntent();
        if (intent != null)setMiddleText(intent.getStringExtra("title"));
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_add_purchase_order;
    }
}