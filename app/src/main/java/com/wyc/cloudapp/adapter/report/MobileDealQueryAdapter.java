package com.wyc.cloudapp.adapter.report;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: MobileDealQueryAdapter
 * @Description: 交易查询适配器
 * @Author: wyc
 * @CreateDate: 2021/2/1 14:24
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/1 14:24
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileDealQueryAdapter extends AbstractTableDataAdapter<MobileDealQueryAdapter.MyViewHolder> {

    private String mOrderCode = "";
    public MobileDealQueryAdapter(MainActivity context) {
        super(context);
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.mobile_deal_query_content_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Utils.dpToPx(mContext,68));
        final int margin = Utils.dpToPx(mContext,24);
        lp.setMarginStart(margin);
        lp.setMarginEnd(margin);
        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (mData != null) {
            final JSONObject object = mData.getJSONObject(position);
            holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("order_code") + "</u>"));
            holder.order_code_tv.setOnClickListener(mItemClickListener);

            holder.order_amt_tv.setText(String.format(Locale.CHINA, "%s%s", object.getString("discount_price"), "元"));
            holder.order_date_tv.setText(object.getString("addtime"));
        }
    }
    @Override
    protected void setCurrentItemView(View v){
        final TextView order_code_tv = (TextView)v;
        mOrderCode = order_code_tv.getText().toString();
        triggerItemClick();
    }

    @Override
    protected JSONObject getCurrentRecord() {
        final JSONObject object = new JSONObject();
        object.put("order_code",mOrderCode);
        return object;
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView order_code_tv,order_amt_tv,order_date_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            order_code_tv =  itemView.findViewById(R.id.order_code_tv);
            order_amt_tv = itemView.findViewById(R.id.order_amt_tv);
            order_date_tv = itemView.findViewById(R.id.order_date_tv);
        }
    }
}
