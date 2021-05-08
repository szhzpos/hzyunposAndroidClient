package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileInventoryAuditAdapter;
import com.wyc.cloudapp.adapter.business.MobileInventoryTaskAdapter;

/*盘点审核*/
public class MobileInventoryOrderActivity extends MobileInventoryTaskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRightText(getString(R.string.space_sz));
        setRightListener(null);
    }

    @Override
    protected AbstractBusinessOrderDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileInventoryAuditAdapter(this);
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileInventoryOrderDetailActivity.class;
    }
}