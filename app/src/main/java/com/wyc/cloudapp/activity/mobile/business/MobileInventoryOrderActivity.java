package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileInventoryTaskAdapter;

/*盘点单*/
public class MobileInventoryOrderActivity extends MobileInventoryTaskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected AbstractBusinessOrderDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileInventoryTaskAdapter(this);
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileInventoryOrderDetailActivity.class;
    }
}