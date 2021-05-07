package com.wyc.cloudapp.activity.mobile.business;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;

public class MobileInventoryOrderDetailActivity extends AbstractMobileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_inventory_order_detail;
    }
}