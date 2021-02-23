package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderAdapter;

public final class MobileWarehouseOrderActivity extends AbstractMobileBusinessOrderActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
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
        return MobileAddWarehouseOrderActivity.class;
    }

    public static class MobileAddWarehouseOrderActivity extends AbstractMobileAddOrderActivity {
        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_warehouse_order;
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
    }
}
