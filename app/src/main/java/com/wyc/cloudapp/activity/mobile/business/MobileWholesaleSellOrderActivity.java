package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;

import org.json.JSONObject;

public class MobileWholesaleSellOrderActivity extends AbstractMobileBusinessOrderActivity {
    @Override
    protected AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return null;
    }

    @Override
    protected JSONObject generateQueryCondition() {
        return null;
    }

    @Override
    protected Class<?> jumpAddTarget() {
        return MobileAddWholesaleSellOrderActivity.class;
    }

    public static class MobileAddWholesaleSellOrderActivity extends AbstractMobileAddOrderActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_wholesale_sell_order;
        }
    }
}
