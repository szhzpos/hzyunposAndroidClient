package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;

public class MobileWholesaleSellOrderActivity extends AbstractMobileBusinessOrderActivity {
    @Override
    protected MobilePurchaseOrderAdapter getAdapter() {
        return null;
    }

    @Override
    protected JSONObject generateQueryCondition() {
        return null;
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddWholesaleSellOrderActivity.class;
    }

    public static class MobileAddWholesaleSellOrderActivity extends AbstractMobileAddOrderActivity {

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
            super.showOrder();
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapter.SuperViewHolder> getAdapter() {
            return null;
        }

        @Override
        protected String generateOrderCodePrefix() {
            return null;
        }

        @Override
        protected JSONObject generateUploadCondition() {
            return null;
        }

        @Override
        protected JSONObject generateAuditCondition() {
            return null;
        }

        @Override
        protected String getOrderIDKey() {
            return null;
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_wholesale_sell_order;
        }
    }
}
