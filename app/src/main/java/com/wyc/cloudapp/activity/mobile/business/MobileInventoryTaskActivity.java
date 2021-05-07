package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileInventoryTaskAdapter;

/*盘点任务*/
public class MobileInventoryTaskActivity extends AbstractMobileBusinessOrderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected AbstractBusinessOrderDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileInventoryTaskAdapter(this);
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put(MobilePracticalInventoryOrderActivity.WH_ID_KEY,getWhId());
        condition.put("api","/api/inventory/task_order_list");
        return condition;
    }

    @Override
    protected String getStatusKey() {
        return "status";
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileInventoryAddTaskActivity.class;
    }

    @Override
    protected String getPermissionId() {
        return "48";
    }
}