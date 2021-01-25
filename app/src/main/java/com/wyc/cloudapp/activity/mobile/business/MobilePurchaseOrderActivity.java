package com.wyc.cloudapp.activity.mobile.business;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.dialog.MyDialog;

import org.json.JSONObject;

public class MobilePurchaseOrderActivity extends AbstractMobileBusinessOrderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return null;
    }

    @Override
    protected JSONObject generateQueryCondition() {
        return null;
    }

    @Override
    protected Class<?> jumpAddTarget() {
        return MobileAddPurchaseOrderActivity.class;
    }

    public static class MobileAddPurchaseOrderActivity extends AbstractMobileAddOrderActivity {

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_purchase_order;
        }
    }
}