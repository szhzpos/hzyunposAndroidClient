package com.wyc.cloudapp.dialog.orderDialog;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.MobileRetailOrderAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.utils.Utils;

public final class MobileQueryRetailOrderDialog extends AbstractMobileQueryDialog {
    public MobileQueryRetailOrderDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.m_sale_query_sz));
    }
    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_retail_order_dialog_layout;
    }

    @Override
    public AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileRetailOrderAdapter(this);
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

    @Override
    public JSONArray getConditionSwitchContent() {
        final JSONArray array = new JSONArray();
        final String search_hint = mContext.getString(R.string.m_search_hint);
        if (search_hint != null){
            final String[] sz = search_hint.split("/");
            for (int i = 0,length = sz.length;i < length;i ++){
                final JSONObject object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,i + 1);
                object.put(TreeListBaseAdapter.COL_NAME,sz[i]);

                array.add(object);
            }
        }
        return array;
    }

}
