package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderAdapter;

public class MobilePurchaseRefundOrderActivity extends AbstractMobileBusinessOrderActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public MobilePurchaseOrderAdapter getAdapter() {
        return null;
    }

    @Override
    protected JSONObject generateQueryCondition() {
        return null;
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddPurchaseRefundOrderActivity.class;
    }

    public static class MobileAddPurchaseRefundOrderActivity extends AbstractMobileAddOrderActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        protected JSONObject generateQueryCondition() {
            return null;
        }

        @Override
        protected void showOrder() {

        }

        @Override
        protected AbstractTableDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
            return null;
        }

        @Override
        protected String generateOrderCodePrefix() {
            return null;
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_purchase_refund_order;
        }
    }
}
