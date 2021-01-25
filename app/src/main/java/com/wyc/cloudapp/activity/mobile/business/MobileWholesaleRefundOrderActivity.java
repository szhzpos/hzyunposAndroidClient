package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;

import org.json.JSONObject;

public class MobileWholesaleRefundOrderActivity extends AbstractMobileBusinessOrderActivity {
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
        return MobileAddWholesaleRefundOrderActivity.class;
    }

    public static class MobileAddWholesaleRefundOrderActivity extends AbstractMobileAddOrderActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_wholesale_refund_order;
        }
    }
}
