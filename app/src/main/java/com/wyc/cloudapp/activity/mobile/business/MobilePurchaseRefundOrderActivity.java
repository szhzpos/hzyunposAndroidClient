package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;

import org.json.JSONObject;

public class MobilePurchaseRefundOrderActivity extends AbstractMobileBusinessOrderActivity {
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
        return MobileAddPurchaseRefundOrderActivity.class;
    }

    public static class MobileAddPurchaseRefundOrderActivity extends AbstractMobileAddOrderActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_purchase_refund_order;
        }
    }
}
