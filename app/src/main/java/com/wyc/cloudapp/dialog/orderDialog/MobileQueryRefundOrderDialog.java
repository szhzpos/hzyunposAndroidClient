package com.wyc.cloudapp.dialog.orderDialog;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.MobileRefundOrderAdapter;
import com.wyc.cloudapp.utils.Utils;

public final class MobileQueryRefundOrderDialog extends AbstractMobileQueryDialog {
    public MobileQueryRefundOrderDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.m_refund_query_sz));
    }
    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_refund_order_dialog_layout;
    }

    @Override
    public AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileRefundOrderAdapter(mContext);
    }

    @Override
    public String generateQueryCondition() {
        final String content = mSearchContent.getText().toString();
        final StringBuilder where_sql = new StringBuilder("where addtime >= "+ mStartTime / 1000 +" and addtime <= "+ mEndTime / 1000);
        if (!content.isEmpty()){
            if (Utils.getViewTagValue(findViewById(R.id.switch_condition),1) == 1){
                where_sql.append(" and order_code like '%").append(content).append("'");
            }else {
                where_sql.append(" and card_code ='").append(content).append("'");
            }
        }
        return where_sql.toString();
    }
}