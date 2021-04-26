package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileTransferInOrderAdapter;

public class MobileTransferInOrderActivity extends AbstractMobileBusinessOrderActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRightText("");
        setRightListener(v -> {});
    }

    @Override
    protected AbstractBusinessOrderDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileTransferInOrderAdapter(this);
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/api_move_in/xlist");
        return condition;
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileTransferInOrderDetailActivity.class;
    }

    @Override
    protected String getPermissionId() {
        return "46";
    }
}