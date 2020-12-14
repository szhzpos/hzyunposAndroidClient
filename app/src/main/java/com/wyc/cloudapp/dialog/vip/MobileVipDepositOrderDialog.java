package com.wyc.cloudapp.dialog.vip;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.MobileChargeOrderAdapter;
import com.wyc.cloudapp.dialog.orderDialog.AbstractMobileQueryDialog;
import com.wyc.cloudapp.utils.Utils;

public class MobileVipDepositOrderDialog extends AbstractMobileQueryDialog {
    public MobileVipDepositOrderDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.m_charge_query_sz));
    }
    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_charge_order_dialog_layout;
    }

    @Override
    public AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileChargeOrderAdapter(mContext);
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
        where_sql.append(" order by addtime desc");
        return where_sql.toString();
    }

    @Override
    public JSONArray getConditionSwitchContent() {
        final JSONArray array = new JSONArray();
        final String search_hint = mContext.getString(R.string.m_charge_search_hint);
        if (search_hint != null){
            final String[] sz = search_hint.split("/");
            for (int i = 0,length = sz.length;i < length;i ++){
                final JSONObject object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",i + 1);
                object.put("item_name",sz[i]);

                array.add(object);
            }
        }
        return array;
    }

}
